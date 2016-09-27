package com.duitang.service.karma.pipe;

/**
 * 移动端网络诊断信息
 *
 * @author kevx
 * @since 11:14:05 AM Apr 7, 2015
 */
public class AppNetworkProbePipe extends CloudPipeBase {

  public void pump(String msg) {
    this.pumpString(msg);
  }

  @Override
  protected String getBiz() {
    return "app_network_probe";
  }
}
