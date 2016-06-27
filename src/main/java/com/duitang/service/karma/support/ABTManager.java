package com.duitang.service.karma.support;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AB Testing核心管理
 *
 * 操作流程： 1:在/napi/settings/埋点，每次应用启动时根据策略条件判定用户用户是否打上abtesing标记，并将标记写入cookie 2:具体的业务代码根据cookie标记来分发
 *
 * @author kevx
 * @since 3:05:23 PM May 13, 2015
 */
public class ABTManager {

  @Resource
  DynamicConfig dynamicConfig;

  public static final String CK_NAME = "abt";

  public static final String DEF_ABTID = "none";

  private SecureRandom srand = new SecureRandom();

  private Cookie fetchOld(HttpServletRequest req) {
    if (req.getCookies() != null) {
      for (Cookie c : req.getCookies()) {
        if (c.getName().equals(CK_NAME)) return c;
      }
    }
    return null;
  }

  /**
   * 获取当前用户正在参与的AB测试项目
   */
  public String currentABT(HttpServletRequest req) {
    Cookie ck = fetchOld(req);
    if (ck != null) {
      return ck.getValue();
    }
    return null;
  }

  /**
   * 根据当前用户条件进行流量分发
   */
  public boolean checkAndDispatch(
      long uid,
      Map<ABTMetaDO.Cond, Boolean> map,
      HttpServletRequest req,
      HttpServletResponse resp) {

    try {
      boolean inWhiteList = isInWhitelist(uid);
      int chosenAge = 10 * 60;
      String abtId = DEF_ABTID;
      if (fetchOld(req) != null && !inWhiteList) {
        extendsCookieAge(10 * 60, req, resp);
        return true;
      }
      ABTMetaDO meta = ABTMetaDO.createFromJson(dynamicConfig.queryItem(ABTMetaDO.abt_meta));
      Preconditions.checkArgument(meta != null && meta.checkValidation());
      ABTMetaDO.ABT abt = meta.fromCondMap(map);
      Preconditions.checkArgument(abt != null);
      if (inWhiteList) {
        chosenAge = meta.leftSeconds();
        abtId = abt.abtId;
      } else if (srand.nextInt(100) < abt.weights) {
        abtId = abt.abtId;
      }

      setCookieAndAttribute(chosenAge, abtId, req, resp);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * 创建dtac令牌用于跟踪
   */
  public String generateDtacToken(HttpServletRequest req) {
    String dtac = req.getParameter("__dtac");
    if (dtac == null) dtac = "{}";
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> m = ABTMetaDO.mapper.readValue(dtac, Map.class);
      if (!m.containsKey("abtid")) {
        String abtid = currentABT(req);
        if (abtid != null) {
          m.put("abtid", abtid);
          dtac = ABTMetaDO.mapper.writeValueAsString(m);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return dtac;
  }

  private boolean isInWhitelist(long uidstr) {
    String str = dynamicConfig.queryItem(ABTMetaDO.abt_whitelist);
    if (str == null || uidstr <= 0) return false;
    String uid = String.valueOf(uidstr);
    List<String> whitelist = Splitter.on(',').splitToList(str);
    if (whitelist.contains(uid)) return true;
    return false;
  }

  public void extendsCookieAge(int age, HttpServletRequest req, HttpServletResponse resp) {
    Cookie old = fetchOld(req);
    setCookieAndAttribute(age, old.getValue(), req, resp);
  }

  private Cookie setCookieAndAttribute(int age, String abtid, HttpServletRequest req, HttpServletResponse resp) {
    Cookie c = new Cookie(CK_NAME, abtid);
    c.setDomain(".duitang.com");
    c.setMaxAge(age);
    c.setPath("/");
    req.setAttribute("abtid", abtid);
    resp.addCookie(c);
    return c;
  }

  public void setDynamicConfig(DynamicConfig dynamicConfig) {
    this.dynamicConfig = dynamicConfig;
  }

}
