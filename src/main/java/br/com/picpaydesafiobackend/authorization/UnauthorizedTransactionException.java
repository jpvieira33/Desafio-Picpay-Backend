package br.com.picpaydesafiobackend.authorization;

public class UnauthorizedTransactionException extends RuntimeException{
    public UnauthorizedTransactionException(String message){
        super(message);
    }
}
