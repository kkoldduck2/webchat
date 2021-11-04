package com.chat.webchat.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

public class ChainedTxConfig {

	/**
	 * @param svcTxManager
	 * @return
	 */
	@Bean
	@Primary
	public PlatformTransactionManager transactionManager(
				@Qualifier("txManagerSvc") PlatformTransactionManager svcTxManager
	) {
		return new ChainedTransactionManager(svcTxManager);
	}

}
