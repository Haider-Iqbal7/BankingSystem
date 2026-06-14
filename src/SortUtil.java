import java.util.ArrayList;
import java.util.Collections;

public class SortUtil {

    // ── Sort accounts by balance ──
    public static void mergeSortByBalance(ArrayList<Accounts> list, boolean ascending) {
        if (list.size() <= 1) return;
        Accounts[] arr = list.toArray(new Accounts[0]);
        Accounts[] temp = new Accounts[arr.length];
        mergeSortBalance(arr, temp, 0, arr.length - 1, ascending);
        list.clear();
        Collections.addAll(list, arr);
    }

    private static void mergeSortBalance(Accounts[] arr, Accounts[] temp, int left, int right, boolean ascending) {
        if (left >= right) return;
        int mid = (left + right) / 2;
        mergeSortBalance(arr, temp, left, mid, ascending);
        mergeSortBalance(arr, temp, mid + 1, right, ascending);
        mergeBalance(arr, temp, left, mid, right, ascending);
    }

    private static void mergeBalance(Accounts[] arr, Accounts[] temp, int left, int mid, int right, boolean ascending) {
        for (int i = left; i <= right; i++) temp[i] = arr[i];
        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right) {
            boolean takeLeft = ascending
                    ? temp[i].getBalance() <= temp[j].getBalance()
                    : temp[i].getBalance() >= temp[j].getBalance();
            arr[k++] = takeLeft ? temp[i++] : temp[j++];
        }
        while (i <= mid)   arr[k++] = temp[i++];
        while (j <= right) arr[k++] = temp[j++];
    }

    // ── Sort transactions by amount ──
    public static void mergeSortByAmount(ArrayList<Transaction> list, boolean ascending) {
        if (list.size() <= 1) return;
        Transaction[] arr = list.toArray(new Transaction[0]);
        Transaction[] temp = new Transaction[arr.length];
        mergeSortAmount(arr, temp, 0, arr.length - 1, ascending);
        list.clear();
        Collections.addAll(list, arr);
    }

    private static void mergeSortAmount(Transaction[] arr, Transaction[] temp, int left, int right, boolean ascending) {
        if (left >= right) return;
        int mid = (left + right) / 2;
        mergeSortAmount(arr, temp, left, mid, ascending);
        mergeSortAmount(arr, temp, mid + 1, right, ascending);
        mergeAmount(arr, temp, left, mid, right, ascending);
    }

    private static void mergeAmount(Transaction[] arr, Transaction[] temp, int left, int mid, int right, boolean ascending) {
        for (int i = left; i <= right; i++) temp[i] = arr[i];
        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right) {
            boolean takeLeft = ascending
                    ? temp[i].getAmount() <= temp[j].getAmount()
                    : temp[i].getAmount() >= temp[j].getAmount();
            arr[k++] = takeLeft ? temp[i++] : temp[j++];
        }
        while (i <= mid)   arr[k++] = temp[i++];
        while (j <= right) arr[k++] = temp[j++];
    }
}
