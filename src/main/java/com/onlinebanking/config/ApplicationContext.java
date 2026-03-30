package com.onlinebanking.config;

import java.util.function.Function;

import javax.sql.DataSource;

import com.onlinebanking.controller.DashboardController;
import com.onlinebanking.controller.LoginController;
import com.onlinebanking.controller.RegisterController;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.BeneficiaryRepository;
import com.onlinebanking.repository.TransactionRepository;
import com.onlinebanking.repository.UserRepository;
import com.onlinebanking.repository.jdbc.JdbcAccountRepository;
import com.onlinebanking.repository.jdbc.JdbcBeneficiaryRepository;
import com.onlinebanking.repository.jdbc.JdbcTransactionRepository;
import com.onlinebanking.repository.jdbc.JdbcUserRepository;
import com.onlinebanking.service.AccountService;
import com.onlinebanking.service.AuthService;
import com.onlinebanking.service.BeneficiaryService;
import com.onlinebanking.service.BillPayService;
import com.onlinebanking.service.TransferService;
import com.onlinebanking.util.DataSourceFactory;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Simple manual dependency graph for controllers, services, and repositories.
 */
public final class ApplicationContext {
    private static final ApplicationContext INSTANCE = new ApplicationContext();

    private final HikariDataSource dataSource;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    private final AuthService authService;
    private final AccountService accountService;
    private final TransferService transferService;
    private final BeneficiaryService beneficiaryService;
    private final BillPayService billPayService;

    private final Function<Class<?>, Object> controllerFactory;

    private ApplicationContext() {
        this.dataSource = DataSourceFactory.createFromProperties();

        this.userRepository = new JdbcUserRepository(dataSource);
        this.accountRepository = new JdbcAccountRepository(dataSource);
        this.transactionRepository = new JdbcTransactionRepository(dataSource);
        this.beneficiaryRepository = new JdbcBeneficiaryRepository(dataSource);

        this.authService = new AuthService(userRepository);
        this.accountService = new AccountService(accountRepository, transactionRepository);
        this.transferService = new TransferService(dataSource, accountRepository, transactionRepository);
        this.beneficiaryService = new BeneficiaryService(beneficiaryRepository);
        this.billPayService = new BillPayService(accountRepository, transactionRepository);

        this.controllerFactory = type -> {
            if (type == LoginController.class) {
                return new LoginController(authService);
            }
            if (type == RegisterController.class) {
                return new RegisterController(authService);
            }
            if (type == DashboardController.class) {
                return new DashboardController(accountService, transferService, beneficiaryService, billPayService);
            }
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Unable to create controller: " + type, e);
            }
        };
    }

    public static ApplicationContext getInstance() {
        return INSTANCE;
    }

    public Function<Class<?>, Object> getControllerFactory() {
        return controllerFactory;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void shutdown() {
        dataSource.close();
    }
}
