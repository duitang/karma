package com.duitang.service.meta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * <pre>
 * {
 * 	"d": "domain name",
 *  "m": "method name",
 *  "p": '[{"type": "XXXX", "value": "{json string}"}, 
 *        {"type": "YYYY", "value": "{json string}"} ... ]',
 *  "r": "{json result}",
 *  "e": "exception msg"
 * }
 * </pre>
 * 
 * @author laurence
 * 
 */
@JsonInclude(Include.NON_NULL)
public class JsonPacket {

	/**
	 * domain name
	 */
	protected String d;

	/**
	 * method name
	 */
	protected String m;

	/**
	 * parameters
	 */
	protected String p;

	/**
	 * return
	 */
	protected Object r;

	/**
	 * exception
	 */
	protected String e;

	public String getD() {
		return d;
	}

	public void setD(String d) {
		this.d = d;
	}

	public String getM() {
		return m;
	}

	public void setM(String m) {
		this.m = m;
	}

	public String getP() {
		return p;
	}

	public void setP(String p) {
		this.p = p;
	}

	public Object getR() {
		return r;
	}

	public void setR(Object r) {
		this.r = r;
	}

	public String getE() {
		return e;
	}

	public void setE(String e) {
		this.e = e;
	}

}
