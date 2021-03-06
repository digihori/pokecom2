package tk.horiuchi.pokecom2;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tk.horiuchi.pokecom2.MainActivity.basic;

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
        int size;
        String body;

        public BasicSource(String s) {

            // 行番号とそれに続く文字列の間にスペースを入れる
            String regex = "^[0-9]+";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(s);
            if (m.find()) {
                s = m.group() + ' ' + s.substring(m.end());
            }
            //Log.w("BasicSource", String.format("s='%s'", s));

            String[] temp = s.split("[\\s:]+", 2);
            //if (temp.length >= 2) {
            //    Log.w("BasicSource", String.format("str1='%s' str2='%s'", temp[0], temp[1]));
            //}
            try {
                lineNum = Integer.parseInt(temp[0]);
            } catch (NumberFormatException e) {

            }
            if (temp.length > 1 && !temp[1].isEmpty()) {
                body = temp[0] + ' ' + temp[1];
                size = 3 + basic.getProgSteps(temp[1]);
            } else {
                body = null;
                size = 0;
            }
            //Log.w("Sourcs", String.format("size=%d", size));
            Log.w("BasicSource", String.format("line=%d body='%s' size=%d", lineNum, body, size));
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
                total += ((BasicSource)list[i].get(j)).size;
            }
        }
        return total;
    }

    public int addSource(int n, String src) {
        if (n > 9) return -1;

        int ret;
        BasicSource temp;
        try {
            temp = new BasicSource(src);
        } catch (NumberFormatException e) {
            Log.w("addSource", "error!!! not linenum!!!");
            return -1;
        }
        if (list[n].contains(temp)) {
            int i = list[n].indexOf(temp);
            if (temp.body != null) {
                // 行の更新
                list[n].set(i, temp);
                ret = 1;
            } else {
                // 行の削除
                list[n].remove(i);
                ret = -1;
            }
        } else {
            if (temp.body != null) {
                // 行の新規追加
                list[n].add(temp);
                ret = 0;
            } else {
                // 削除しようとしている行番号が存在しない場合は何もしない
                Log.w("addSource", String.format("Not found the linenum(%d).\n", temp.lineNum));
                return(-1);
            }
        }
        Collections.sort(list[n], new MyComparator());
        idx[n] = list[n].indexOf(temp);
        return ret;
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
            //Log.w("load", String.format("size=%d src='%s'", ((BasicSource)list[n].get(i)).size, src[i]));
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

    public boolean setSourceAll(int n, String[] src) {
        int ret = 0;
        for (int i = 0; i < src.length; i++) {
            ret = addSource(n, src[i]);
            if (ret != 0) break;
        }

        return (ret == 0 ? true : false);
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
        if (idx[n] >= size) {
            idx[n] = size - 1;
            return null;
        }
        return ((BasicSource)list[n].get(idx[n])).body;
    }

    public String getSourcePrev(int n) {
        if (n > 9) return null;
        int size = list[n].size();
        if (size == 0) return null;
        idx[n]--;
        if (idx[n] < 0) {
            idx[n] = 0;
            return null;
        }
        return ((BasicSource)list[n].get(idx[n])).body;
    }

    public String getSource(int n, int m) {
        if (n > 9) return null;
        int size = list[n].size();
        if (size == 0) return null;
        BasicSource temp = new BasicSource(String.valueOf(m));
        int i = list[n].indexOf(temp);
        if (i != -1) {
            idx[n] = i;
        } else {
            idx[n] = 0;
        }
        //if (idx[n] < 0) idx[n] = 0;
        //if (idx[n] >= size) idx[n] = size - 1;
        return ((BasicSource)list[n].get(idx[n])).body;
    }

    public String getSource1(int n, int m) {
        if (n > 9) return null;
        int size = list[n].size();
        if (size == 0) return null;

        int idx;
        for (idx = 0; idx < list[n].size(); idx++) {
            if (m <= ((BasicSource)list[n].get(idx)).lineNum) {
                m = ((BasicSource)list[n].get(idx)).lineNum;
                Log.w("getSource1", String.format("target=%d", m));
                break;
            }
        }
        if (idx >= list[n].size()) {
            return null;
        } else {
            return getSource(n, m);
        }
    }

    public String getCurrentSource(int n) {
        if (n > 9) return null;
        int size = list[n].size();
        if (size == 0) return null;
        return ((BasicSource)list[n].get(idx[n])).body;
    }

}
