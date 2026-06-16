import java.util.LinkedList;

public class QueueService {
    private BankDatabase db;

    public QueueService() {
        this.db = BankDatabase.getInstance();
    }

    public QueueToken issueToken(String customerName, String accountNumber,
                                 QueueToken.ServiceType serviceType,
                                 QueueToken.Priority priority) {
        if (customerName == null || customerName.isEmpty()) {
            customerName = "Walk-In Customer";
        }
        QueueToken token = new QueueToken(customerName, accountNumber, serviceType, priority);
        db.enqueueToken(token);
        return token;
    }

    public QueueToken serveNext() {
        return db.dequeueToken();
    }

    public QueueToken peekNext() {
        return db.peekQueue();
    }

    public LinkedList<QueueToken> getCurrentQueue() {
        return db.getQueue();
    }

    public int getQueueSize() {
        return db.getQueueSize();
    }
    public String cancelToken(String tokenNumber) {
        LinkedList<QueueToken> queue = db.getQueue();
        for (QueueToken token : queue) {
            if (token.getTokenNumber().equals(tokenNumber)) {
                if (token.getStatus() == QueueToken.TokenStatus.Waiting) {
                    token.markCancelled();
                    queue.remove(token);
                    return "SUCCESS: Token " + tokenNumber + " cancelled!";
                }
                return "ERROR: Token is already being served or completed!";
            }
        }
        return "ERROR: Token not found in queue!";
    }

    public int getEstimatedWaitTime(String tokenNumber) {
        LinkedList<QueueToken> queue = db.getQueue();
        int position = 0;
        for (QueueToken token : queue) {
            if (token.getTokenNumber().equals(tokenNumber)) {
                return position * 5; // Assume 5 mins per customer
            }
            position++;
        }
        return -1;
    }
}


