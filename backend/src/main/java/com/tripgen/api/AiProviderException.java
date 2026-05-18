package com.tripgen.api;

public class AiProviderException extends RuntimeException {

    private final String providerName;
    private final boolean quotaOrBillingLimit;

    public AiProviderException(String providerName, String message, boolean quotaOrBillingLimit) {
        super(message);
        this.providerName = providerName;
        this.quotaOrBillingLimit = quotaOrBillingLimit;
    }

    public AiProviderException(String providerName, String message, boolean quotaOrBillingLimit, Throwable cause) {
        super(message, cause);
        this.providerName = providerName;
        this.quotaOrBillingLimit = quotaOrBillingLimit;
    }

    public String getProviderName() {
        return providerName;
    }

    public boolean isQuotaOrBillingLimit() {
        return quotaOrBillingLimit;
    }
}
