package br.com.picpaydesafiobackend.wallet;

public enum WalletType {
    COMUM(1), LOGISTA(2);

    final private int value;

    private WalletType(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
