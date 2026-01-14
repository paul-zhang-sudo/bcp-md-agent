package com.bsi.factory;

import java.security.KeyPair;

/**
 * KeyPairs 主键对
 */
public class KeyPairs {

	private final KeyPair keyPair;
	
	public KeyPairs(KeyPair keyPair){
		this.keyPair = keyPair;
	}
	
	public String getPublicKey(){
		return Base64Util.encryptBASE64(keyPair.getPublic().getEncoded());
	}
	
	public String getPrivateKey(){
		return Base64Util.encryptBASE64(keyPair.getPrivate().getEncoded());
	}
}
