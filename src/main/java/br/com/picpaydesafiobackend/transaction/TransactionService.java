package br.com.picpaydesafiobackend.transaction;

import br.com.picpaydesafiobackend.authorization.AuthorizationService;
import br.com.picpaydesafiobackend.notification.NotificationService;
import br.com.picpaydesafiobackend.wallet.Wallet;
import br.com.picpaydesafiobackend.wallet.WalletRepository;
import br.com.picpaydesafiobackend.wallet.WalletType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository,
                              AuthorizationService authorizationService, NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Transaction create(Transaction transaction){

        //validar
        validate(transaction);

         //criar transação
        var newTransaction = transactionRepository.save(transaction);

        // debitar da carteira
        var walletPayer = walletRepository.findById(transaction.payer()).get();
        var walletPayee = walletRepository.findById(transaction.payee()).get();
        walletRepository.save(walletPayer.debit(transaction.value()));
        walletRepository.save(walletPayee.credit(transaction.value()));

        //chamar serviços externos
          //autorização de transação
          authorizationService.authorize(transaction);

          //notificacao
        notificationService.notify(transaction);

        return newTransaction;
    }

    private void validate(Transaction transaction) {
        walletRepository.findById(transaction.payee())
                .map(payee -> walletRepository.findById(transaction.payer())
                  .map(payer -> isTransactionValid(transaction, payer)  ? transaction: null)
                  .orElseThrow(() -> new InvalidTransactionException("Invalid Transaction - %s".formatted(transaction))))
                .orElseThrow(() -> new InvalidTransactionException("Invalid Transaction - %s".formatted(transaction)));
    }

    private boolean isTransactionValid(Transaction transaction, Wallet payer){
        return payer.type() == WalletType.COMUM.getValue() &&
                payer.balance().compareTo(transaction.value()) >= 0 &&
                !payer.id().equals(transaction.payee());
    }

    public List<Transaction> list() {
        return transactionRepository.findAll();
    }
}
