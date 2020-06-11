package com.lxz.bank.account;

import java.util.concurrent.locks.StampedLock;

public class BankAccount {
    private final StampedLock lock = new StampedLock();
    private long balance;

    public long getBalanceOptimistically() {
        long stamp = lock.tryOptimisticRead();
        long localBalance = balance;

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                localBalance = balance;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return localBalance;
    }

    public long getBalance() {
        long stamp = lock.readLock();
        try {
            return balance;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public void deposit(long amount) {
        long stamp = lock.writeLock();

        try {
            balance += amount;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void withdraw(long amount) {
        long stamp = lock.writeLock();

        try {
            balance -= amount;
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
