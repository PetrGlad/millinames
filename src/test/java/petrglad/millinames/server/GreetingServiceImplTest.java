package petrglad.millinames.server;

import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static petrglad.millinames.server.NameServiceImpl.genName;

public class GreetingServiceImplTest {

//    @Test
//    public void testInMemorySort() {
//        char[] buffer = new char[10];
//        final Random r = new Random();
//        long t = System.currentTimeMillis();
//        int N = 1000000;
//        final String[][] records = new String[N][2];
//        for (int i = 0; i < records.length; i++) {
//            records[i] = new String[]{genName(r, buffer), genName(r, buffer)};
//        }
//        System.out.println(System.currentTimeMillis() - t);
//        sort0(records);
//        sort1(records);
//        sort0(records);
//        sort1(records);
//        sort0(records);
//        sort1(records);
//        sort0(records);
//        sort1(records);
//
//        Integer[] sort0i = new Integer[N];
//        sortIndexed(records, sort0i, 0);
//
//        Integer[] sort1i = new Integer[N];
//        sortIndexed(records, sort1i, 1);
//    }
//
//    private void sortIndexed(final String[][] records, Integer[] sort0i, final int p) {
//        long tt = System.currentTimeMillis();
//        for (int i = 0; i < records.length; i++) {
//            sort0i[i] = i;
//        }
//        Arrays.sort(sort0i, new Comparator<Integer>() {
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                return records[o1][p].compareTo(records[o2][p]);
//            }
//        });
//        System.out.println(System.currentTimeMillis() - tt);
//    }
//
//    private void sort0(String[][] records) {
//        long t = System.currentTimeMillis();
//        Arrays.sort(records, new Comparator<Object>() {
//            @Override
//            public int compare(Object o1, Object o2) {
//                return ((String[]) o1)[0].compareTo(((String[]) o2)[0]);
//            }
//        });
//        System.out.println(System.currentTimeMillis() - t);
//    }
//
//    private void sort1(String[][] records) {
//        long t = System.currentTimeMillis();
//        Arrays.sort(records, new Comparator<Object>() {
//            @Override
//            public int compare(Object o1, Object o2) {
//                return ((String[]) o1)[1].compareTo(((String[]) o2)[1]);
//            }
//        });
//        System.out.println(System.currentTimeMillis() - t);
//    }
}
