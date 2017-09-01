package com.lfy.disklrucachedemo;

public class TicketRunnable implements Runnable {
    private int ticketCount = 50;

    @Override
    public void run() {
        while (ticketCount > 0) {
            synchronized (this) {
                System.out.println(Thread.currentThread().getName() + " 卖出第" + (50 - ticketCount + 1) + "张票");
                ticketCount--;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
