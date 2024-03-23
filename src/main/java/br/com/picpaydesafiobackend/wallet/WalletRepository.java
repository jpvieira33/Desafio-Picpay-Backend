package br.com.picpaydesafiobackend.wallet;

import org.springframework.data.repository.CrudRepository;

public interface WalletRepository extends CrudRepository<Wallet, Long> {
}
