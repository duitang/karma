package com.duitang.service.karma.meta;

import io.netty.channel.ChannelHandlerContext;

import java.nio.ByteBuffer;

/***
 * Binary Packet Format:
 *
 *
 * <pre>
 * int = 4 bytes
 * float = 4 bytes
 * bytes = variable length
 * field, state code
 *
 * ---------------------------------------------------------
 * MAGIC_CODE1(2 bytes)|  1
 * ---------------------------------------------------------
 * TOTAL(int)          |  1
 * ---------------------------------------------------------
 * CHECKSUM(long)      |  1
 * ---------------------------------------------------------
 * VERSION(float)      |  1
 * ---------------------------------------------------------
 * FLAG(int)           |  1
 * ---------------------------------------------------------
 * UUID(long)          |  1
 * ---------------------------------------------------------
 * CONFIG_SIZE(int)    |  3
 * ---------------------------------------------------------
 * CONFIG(bytes)       |  4
 * ---------------------------------------------------------
 * DOMAIN_SIZE(int)    |  5
 * ---------------------------------------------------------
 * DOMAIN(bytes)       |  6
 * ---------------------------------------------------------
 * METHOD_SIZE(int)    |  7
 * ---------------------------------------------------------
 * METHOD(bytes)       |  8
 * ---------------------------------------------------------
 * PARAMETER_SIZE(int) |  9
 * ---------------------------------------------------------
 * PARAMETER(bytes)    |  10
 * ---------------------------------------------------------
 * RETURN_SIZE(int)    |  11
 * ---------------------------------------------------------
 * RETURN(bytes)       |  12
 * ---------------------------------------------------------
 * EXCEPTION_SIZE(int) |  13
 * ---------------------------------------------------------
 * EXCEPTION(bytes)    |  14
 * ---------------------------------------------------------
 * </pre>
 *
 * @author laurence
 */
public class BinaryPacketRaw implements Packet {

  protected int total = -1;
  protected float version = 1.0f;
  protected int flag = -1;
  protected long uuid = 0;
  protected int szConf = -1;
  protected ByteBuffer conf;
  protected int szDomainName = -1;
  protected ByteBuffer domainName;
  protected int szMethodName = -1;
  protected ByteBuffer methodName;
  protected int szParameter = -1;
  protected ByteBuffer parameter;
  protected int szRet = -1;
  protected ByteBuffer ret;
  protected int szError = -1;
  protected ByteBuffer error;

  public ChannelHandlerContext ctx;

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public float getVersion() {
    return version;
  }

  public void setVersion(float version) {
    this.version = version;
  }

  public int getFlag() {
    return flag;
  }

  public void setFlag(int flag) {
    this.flag = flag;
  }

  public long getUuid() {
    return uuid;
  }

  public void setUuid(long uuid) {
    this.uuid = uuid;
  }

  public int getSzConf() {
    return szConf;
  }

  public void setSzConf(int szConf) {
    this.szConf = szConf;
  }

  public ByteBuffer getConf() {
    return conf;
  }

  public void setConf(ByteBuffer conf) {
    this.conf = conf;
  }

  public int getSzDomainName() {
    return szDomainName;
  }

  public void setSzDomainName(int szDomainName) {
    this.szDomainName = szDomainName;
  }

  public ByteBuffer getDomainName() {
    return domainName;
  }

  public void setDomainName(ByteBuffer domainName) {
    this.domainName = domainName;
  }

  public int getSzMethodName() {
    return szMethodName;
  }

  public void setSzMethodName(int szMethodName) {
    this.szMethodName = szMethodName;
  }

  public ByteBuffer getMethodName() {
    return methodName;
  }

  public void setMethodName(ByteBuffer methodName) {
    this.methodName = methodName;
  }

  public int getSzParameter() {
    return szParameter;
  }

  public void setSzParameter(int szParameter) {
    this.szParameter = szParameter;
  }

  public ByteBuffer getParameter() {
    return parameter;
  }

  public void setParameter(ByteBuffer parameter) {
    this.parameter = parameter;
  }

  public int getSzRet() {
    return szRet;
  }

  public void setSzRet(int szRet) {
    this.szRet = szRet;
  }

  public ByteBuffer getRet() {
    return ret;
  }

  public void setRet(ByteBuffer ret) {
    this.ret = ret;
  }

  public int getSzError() {
    return szError;
  }

  public void setSzError(int szError) {
    this.szError = szError;
  }

  public ByteBuffer getError() {
    return error;
  }

  public void setError(ByteBuffer error) {
    this.error = error;
  }

}
