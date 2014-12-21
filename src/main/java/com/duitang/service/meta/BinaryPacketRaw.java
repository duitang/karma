package com.duitang.service.meta;

import java.nio.ByteBuffer;

import org.apache.mina.core.session.IoSession;

/***
 * Binary Packet Format:
 * 
 * 
 * <pre>
 * int = 4 bytes 
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
 * FLAG(int)           |  1
 * --------------------------------------------------------- 
 * UUID(long)          |  4
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
	protected int flag = -1;
	protected long uuid = 0;
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

	public IoSession iochannel;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
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
