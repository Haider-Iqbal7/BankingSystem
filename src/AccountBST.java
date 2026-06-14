import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;

public class AccountBST {
    Node root;
    int size;

    private static class Node {
        double balanceKey;
        Accounts accounts;
        Node left, right;

        Node(Accounts accounts) {
            this.balanceKey = accounts.getBalance();
            this.accounts = accounts;
        }
    }
    public int size() { return size; }
    public boolean isEmpty() { return root == null; }

    // ── Insert ──
    public void insert(Accounts account) {
        root = insertRec(root, account);
        size++;
    }

    private Node insertRec(Node node, Accounts account) {
        if (node == null) return new Node(account);
        if (account.getBalance() < node.balanceKey)
            node.left = insertRec(node.left, account);
        else
            node.right = insertRec(node.right, account);
        return node;
    }
    // ── Search: exact balance match ──
    public Accounts search(double balance) {
        Node cur = root;
        while (cur != null) {
            if (balance == cur.balanceKey) return cur.accounts;
            cur = (balance < cur.balanceKey) ? cur.left : cur.right;
        }
        return null;
    }
    // ── Range Query: accounts with balance in [min, max] ──
    public ArrayList<Accounts> rangeQuery(double min, double max) {
        ArrayList<Accounts> result = new ArrayList<>();
        rangeQueryRec(root, min, max, result);
        return result;
    }

    private void rangeQueryRec(Node node, double min, double max, ArrayList<Accounts> result) {
        if (node == null) return;
        if (node.balanceKey > min) rangeQueryRec(node.left, min, max, result);
        if (node.balanceKey >= min && node.balanceKey <= max) result.add(node.accounts);
        if (node.balanceKey < max) rangeQueryRec(node.right, min, max, result);
    }
    // ── In-order traversal: ascending order by balance ──
    public ArrayList<Accounts> inOrder() {
        ArrayList<Accounts> result = new ArrayList<>();
        inOrderRec(root, result);
        return result;
    }

    private void inOrderRec(Node node, ArrayList<Accounts> result) {
        if (node == null) return;
        inOrderRec(node.left, result);
        result.add(node.accounts);
        inOrderRec(node.right, result);
    }

    // ── Find Min / Max balance account ──
    public Accounts findMin() {
        if (root == null) return null;
        Node cur = root;
        while (cur.left != null) cur = cur.left;
        return cur.accounts;
    }

    public Accounts findMax() {
        if (root == null) return null;
        Node cur = root;
        while (cur.right != null) cur = cur.right;
        return cur.accounts;
    }
    // ── Height of the tree ──
    public int height() { return heightRec(root); }

    private int heightRec(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(heightRec(node.left), heightRec(node.right));
    }

    // ── Rebuild tree from a fresh collection (call after balances change) ──
    public void rebuild(Collection<Accounts> account) {
        root = null;
        size = 0;
        for (Accounts a : account) insert(a);
    }
}



