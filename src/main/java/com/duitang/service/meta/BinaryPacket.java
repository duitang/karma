package com.duitang.service.meta;

import java.nio.ByteBuffer;

/***
 * Binary Packet Format:
 * 
 * <pre>
 * ----------------------------------------------------- 
 * MAGIC_CODE | TOTAL(long) |
 * --------------------------------------------------------- 
 * MAGIC_CODE | FLAG(long) |
 * --------------------------------------------------------- 
 * MAGIC_CODE | DOMAIN_SIZE(long)    | DOMAIN(bytes)    |
 * --------------------------------------------------------- 
 * MAGIC_CODE | METHOD_SIZE(long)    | METHOD(bytes)    |
 * ---------------------------------------------------------
 * MAGIC_CODE | PARAMETER_SIZE(long) | PARAMETER(bytes) |
 * --------------------------------------------------------- 
 * MAGIC_CODE | RETURN_SIZE(long)    | RETURN(bytes)    |
 * --------------------------------------------------------- 
 * MAGIC_CODE | EXCEPTION_SIZE(long) | EXCEPTION(bytes) |
 * --------------------------------------------------------- 
 * MAGIC_CODE | MAGIC_CODE |                       
 * ---------------------------------------------------------
 * </pre>
 * 
 * @author laurence
 */
public class BinaryPacket implements Packet {

	protected long total;
	protected long flag;
	protected ByteBuffer domainName;
	protected ByteBuffer methodName;
	protected ByteBuffer parameter;
	protected ByteBuffer ret;
	protected ByteBuffer error;

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getFlag() {
		return flag;
	}

	public void setFlag(long flag) {
		this.flag = flag;
	}

	public ByteBuffer getDomainName() {
		return domainName;
	}

	public void setDomainName(ByteBuffer domainName) {
		this.domainName = domainName;
	}

	public ByteBuffer getMethodName() {
		return methodName;
	}

	public void setMethodName(ByteBuffer methodName) {
		this.methodName = methodName;
	}

	public ByteBuffer getParameter() {
		return parameter;
	}

	public void setParameter(ByteBuffer parameter) {
		this.parameter = parameter;
	}

	public ByteBuffer getRet() {
		return ret;
	}

	public void setRet(ByteBuffer ret) {
		this.ret = ret;
	}

	public ByteBuffer getError() {
		return error;
	}

	public void setError(ByteBuffer error) {
		this.error = error;
	}

}
