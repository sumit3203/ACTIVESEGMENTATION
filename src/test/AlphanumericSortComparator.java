package test;

import java.io.File;
import java.util.Comparator;

//Java alphanumeric sort
//unsorted array:     [10, 1, 2]
//simple Arrays.sort: [1, 10, 2]
//alphanumeric sort:  [1, 2, 10]
// https://dirask.com/posts/Java-how-to-sort-alphanumeric-array-of-Strings-natural-order-sorting-VDKErj
public final class AlphanumericSortComparator<T> implements  Comparator<T> {

 public static final Comparator<File> NUMERICAL_ORDER
         = new AlphanumericSortComparator<>(false);
 public static final Comparator<File> CASE_INSENSITIVE_NUMERICAL_ORDER
         = new AlphanumericSortComparator<>(true);

 private final boolean caseInsensitive;

 public AlphanumericSortComparator(boolean caseInsensitive) {
     this.caseInsensitive = caseInsensitive;
 }

 int compareRight(String a, String b) {
     int bias = 0;
     int ia = 0;
     int ib = 0;

     // The longest run of digits wins.  That aside, the greatest
     // value wins, but we can't know that it will until we've scanned
     // both numbers to know that they have the same magnitude, so we
     // remember it in BIAS.
     for (;; ia++, ib++) {
         char ca = charAt(a, ia);
         char cb = charAt(b, ib);

         if (!Character.isDigit(ca) && !Character.isDigit(cb)) {
             return bias;
         } else if (!Character.isDigit(ca)) {
             return -1;
         } else if (!Character.isDigit(cb)) {
             return +1;
         } else if (ca < cb) {
             if (bias == 0) {
                 bias = -1;
             }
         } else if (ca > cb) {
             if (bias == 0)
                 bias = +1;
         } else if (ca == 0 && cb == 0) {
             return bias;
         }
     }
 }

 public int compare(T o1, T o2) {
     String a = o1.toString();
     String b = o2.toString();

     int ia = 0, ib = 0;
     int nza = 0, nzb = 0;
     char ca, cb;
     int result;

     while (true) {
         // only count the number of zeroes leading the last number compared
         nza = nzb = 0;

         ca = charAt(a, ia);
         cb = charAt(b, ib);

         // skip over leading zeros
         while (ca == '0') {
             if (ca == '0') {
                 nza++;
             } else {
                 // only count consecutive zeroes
                 nza = 0;
             }

             // if the next character isn't a digit, then we've had a run
             // of only zeros
             // we still need to treat this as a 0 for comparison purposes
             if (!Character.isDigit(charAt(a, ia+1)))
                 break;

             ca = charAt(a, ++ia);
         }

         while (cb == '0') {
             if (cb == '0') {
                 nzb++;
             } else {
                 // only count consecutive zeroes
                 nzb = 0;
             }

             // if the next character isn't a digit, then we've had a run
             // of only zeros
             // we still need to treat this as a 0 for comparison purposes
             if (!Character.isDigit(charAt(b, ib+1)))
                 break;

             cb = charAt(b, ++ib);
         }

         // process run of digits
         if (Character.isDigit(ca) && Character.isDigit(cb)) {
             if ((result = compareRight(a.substring(ia), b
                     .substring(ib))) != 0) {
                 return result;
             }
         }

         if (ca == 0 && cb == 0) {
             // The strings compare the same.  Perhaps the caller
             // will want to call strcmp to break the tie.
             return nza - nzb;
         }

         if (ca < cb) {
             return -1;
         } else if (ca > cb) {
             return +1;
         }

         ++ia;
         ++ib;
     }
 }

 private char charAt(String s, int i) {
     if (i >= s.length()) {
         return 0;
     } else {
         return caseInsensitive ?
                 Character.toUpperCase(s.charAt(i)) : s.charAt(i);
     }
 }
}