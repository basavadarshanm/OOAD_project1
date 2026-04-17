package com.onlinebanking.config;

import java.lang.reflect.InvocationTargetException;
import javax.sql.DataSource;

import javafx.util.Callback;

import com.onlinebanking.controller.DashboardController;
import com.onlinebanking.controller.LoginController;
import com.onlinebanking.controller.PayBillsController;
import com.onlinebanking.controller.RegisterController;
import com.onlinebanking.controller.TransferMoneyController;
import com.onlinebanking.repository.AccountRepository;
import com.onlinebanking.repository.BeneficiaryRepository;
import com.onlinebanking.repository.BillPaymentRepository;
import com.onlinebanking.repository.TransactionRepository;
import com.onlinebanking.repository.UserRepository;
import com.onlinebanking.repository.jdbc.JdbcAccountRepository;
import com.onlinebanking.repository.jdbc.JdbcBeneficiaryRepository;
import com.onlinebanking.repository.jdbc.JdbcBillPaymentRepository;
import com.onlinebanking.repository.jdbc.JdbcTransactionRepository;
import com.onlinebanking.repository.jdbc.JdbcUserRepository;
import com.onlinebanking.service.AccountService;
import com.onlinebanking.service.AuthService;
import com.onlinebanking.service.BeneficiaryService;
import com.onlinebanking.service.BillPayService;
import com.onlinebanking.service.ManagerService;
import com.onlinebanking.service.ReceiptService;
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
    private final BillPaymentRepository billPaymentRepository;

    private final AuthService authService;
    private final AccountService accountService;
    private final TransferService transferService;
    private final BeneficiaryService beneficiaryService;
    private final BillPayService billPayService;
    private final ManagerService managerService;
    private final ReceiptService receiptService;

    private final Callback<Class<?>, Object> controllerFactory;

    private ApplicationContext() {
        this.dataSource = DataSourceFactory.createFromProperties();

        this.userRepository = new JdbcUserRepository(dataSource);
        this.accountRepository = new JdbcAccountRepository(dataSource);
        this.transactionRepository = new JdbcTransactionRepository(dataSource);
        this.beneficiaryRepository = new JdbcBeneficiaryRepository(dataSource);
        this.billPaymentRepository = new JdbcBillPaymentRepository(dataSource);

        this.authService = new AuthService(userRepository);
        this.accountService = new AccountService(accountRepository, transactionRepository);
        this.transferService = new TransferService(dataSource, accountRepository, transactionRepository);
        this.beneficiaryService = new BeneficiaryService(beneficiaryRepository);
        this.billPayService = new BillPayService(accountRepository, billPaymentRepository, transactionRepository);
        this.managerService = new ManagerService(userRepository, transactionRepository);
        this.receiptService = new ReceiptService();

        this.controllerFactory = new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> type) {
            if (type == LoginController.class) {
                return new LoginController(authService);
            }
            if (type == RegisterController.class) {
                return new RegisterController(authService);
            }
            if (type == DashboardController.class) {
                return new DashboardController(accountService, transferService, beneficiaryService, billPayService);
            }
            if (type == TransferMoneyController.class) {
                return new TransferMoneyController(accountService, transferService, receiptService);
            }
            if (type == PayBillsController.class) {
                return new PayBillsController(billPayService, receiptService);
            }
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalStateException("Unable to create controller: " + type, e);
            }
            }
        };
    }

    public static ApplicationContext getInstance() {
        return INSTANCE;
    }

    public Callback<Class<?>, Object> getControllerFactory() {
        return controllerFactory;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    // Getters for services (needed by controllers)
    public AuthService getAuthService() {
        return authService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public TransferService getTransferService() {
        return transferService;
    }

    public BeneficiaryService getBeneficiaryService() {
        return beneficiaryService;
    }

    public BillPayService getBillPayService() {
        return billPayService;
    }

    public ManagerService getManagerService() {
        return managerService;
    }

    public ReceiptService getReceiptService() {
        return receiptService;
    }

    public void shutdown() {
        dataSource.close();
    }
}
