package it.unibo.oop.lab.workers02;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MultiThreadedSumMatrix implements SumMatrix {

    private final int nthread;

    public MultiThreadedSumMatrix(final int nthread) {
        this.nthread = nthread;
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startpos;
        private final int nelem;
        private long res;

        /**
         * Build a new worker.
         * 
         * @param list
         *            the list to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final double[][] matrix, final int startpos, final int nelem) {
            super();
            this.matrix = matrix;
            this.startpos = startpos;
            this.nelem = nelem;
        }

        @Override
        public void run() {
            final int lineLength = Array.getLength(matrix[0]);
            final int totalSize = lineLength * lineLength;
            System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1));
            for (int i = startpos; i < totalSize && i < startpos + nelem; i++) {
                for (int j = startpos; j < lineLength && j < startpos + nelem; j++) {
                    this.res += this.matrix[i][j];
                }
            }
        }

        /**
         * Returns the result of summing up the integers within the list.
         * 
         * @return the sum of every element in the array
         */
        public long getResult() {
            return this.res;
        }

    }

    @Override
    public double sum(final double[][] matrix) {
        final int lineLength = Array.getLength(matrix[0]);
        final int totalSize = lineLength * lineLength;
        final int size = totalSize / this.nthread + totalSize % this.nthread;
        /*
         * Build a list of workers
         */
        final List<Worker> workers = new ArrayList<>(nthread);
        for (int start = 0; start < totalSize; start += size) {
            workers.add(new Worker(matrix, start, size));
        }
        /*
         * Start them
         */
        for (final Worker w: workers) {
            w.start();
        }
        /*
         * Wait for every one of them to finish. This operation is _way_ better done by
         * using barriers and latches, and the whole operation would be better done with
         * futures.
         */
        long sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        /*
         * Return the sum
         */
        return sum;
    }

}
