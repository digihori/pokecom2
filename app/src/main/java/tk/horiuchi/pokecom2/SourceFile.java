package tk.horiuchi.pokecom2;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by yoshimine on 2017/12/03.
 */

public class SourceFile {
    private List[] list;
    private int[] idx;

    public SourceFile() {
        list = new List[] {
                new ArrayList<BasicSource>(),
                new ArrayList<BasicSource>(),
                new ArrayList<BasicSource>(),
                new ArrayList<BasicSource>(),
                new ArrayList<BasicSource>(),
                new ArrayList<BasicSource>(),
                new ArrayList<BasicSource>(),
                new ArrayList<BasicSource>(),
                new ArrayList<BasicSource>(),
                new ArrayList<BasicSource>()
        };

        idx = new int[10];

        Log.w("SourceFile", String.format("list[0].size=%d", list[0].size()));
    }

    class BasicSource {
        int lineNum;
        String body;

        public BasicSource(String s) {
            String[] temp = s.split("[\\s+:]", 2);
            lineNum = Integer.parseInt(temp[0]);
            if (temp.length > 1) {
                body = s;
            } else {
                body = null;
            }
        }

        public boolean equals(Object obj) {
            return (this.lineNum == ((BasicSource)obj).lineNum);
        }

    }

    private class MyComparator implements Comparator<BasicSource> {
        @Override
        public int compare(BasicSource o1, BasicSource o2) {
            return Double.compare(o1.lineNum, o2.lineNum);
        }

    }

    public boolean checkProgExist(int n) {
        if (n > 9) return false;
        Log.w("SourceFile", String.format("list[%d].size=%d", n, list[n].size()));
        return (list[n].size() == 0 ? false : true);
    }

    public int getUsedMemorySize() {
        int total = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < list[i].size(); j++) {
                total += ((BasicSource)list[i].get(j)).body.length();
            }
        }
        return total;
    }

    public void addSource(int n, String src) {
        if (n > 9) return;

        BasicSource temp = new BasicSource(src);
        if (list[n].contains(temp)) {
            int i = list[n].indexOf(temp);
            if (temp.body != null) {
                list[n].set(i, temp);
            } else {
                list[n].remove(i);
            }
        } else {
            list[n].add(temp);
        }
        Collections.sort(list[n], new MyComparator());
    }

    public void clearSource(int n) {
        if (n > 9) return;
        list[n].clear();
    }

    public void clearSourceAll() {
        for (int n = 0; n < 10; n++) {
            list[n].clear();
        }
    }

    public void loadSource(int n, String[] src) {
        if (n > 9) return;

        list[n].clear();
        for (int i = 0; i < src.length; i++) {
            //Log.w("loadSource", src[i]);
            list[n].add(new BasicSource(src[i]));
        }
        Collections.sort(list[n], new MyComparator());
    }

    public String[] getSourceAll(int n) {
        int length = list[n].size();
        if (length == 0) return null;

        String[] src = new String[length];
        int i = 0;

        for (Iterator it = list[n].iterator(); it.hasNext(); ) {
            src[i++] = ((BasicSource)it.next()).body;
        };

        return src;
    }

    public String getSourceTop(int n) {
        if (n > 9) return null;
        if (list[n].size() == 0) return null;
        idx[n] = 0;
        return ((BasicSource)list[n].get(idx[n])).body;
    }

    public String getSourceBottom(int n) {
        if (n > 9) return null;
        if (list[n].size() == 0) return null;
        idx[n] = list[n].size() - 1;
        return ((BasicSource)list[n].get(idx[n])).body;
    }

    public String getSourceNext(int n) {
        if (n > 9) return null;
        int size = list[n].size();
        if (size == 0) return null;
        idx[n]++;
        if (idx[n] >= size) idx[n] = size - 1;
        return ((BasicSource)list[n].get(idx[n])).body;
    }

    public String getSourcePrev(int n) {
        if (n > 9) return null;
        int size = list[n].size();
        if (size == 0) return null;
        idx[n]--;
        if (idx[n] < 0) idx[n] = 0;
        return ((BasicSource)list[n].get(idx[n])).body;
    }

    public String getSource(int n, int m) {
        if (n > 9) return null;
        int size = list[n].size();
        if (size == 0) return null;
        idx[n] = m;
        if (idx[n] < 0) idx[n] = 0;
        if (idx[n] >= size) idx[n] = size - 1;
        return ((BasicSource)list[n].get(idx[n])).body;
    }

}
