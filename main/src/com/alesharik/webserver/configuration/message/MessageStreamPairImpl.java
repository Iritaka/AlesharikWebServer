/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.configuration.message;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

//TODO rewrite
public class MessageStreamPairImpl<M extends Message> implements MessageStreamPair<M, MessageStreamPairImpl.MessageStreamImpl> {
    private final MessageStreamImpl<M> first;
    private final MessageStreamImpl<M> second;

    public MessageStreamPairImpl() {
        MpscAtomicArrayQueue<M> firstQueue = new MpscAtomicArrayQueue<>(512);
        MpscAtomicArrayQueue<M> secondQueue = new MpscAtomicArrayQueue<>(512);

        first = new MessageStreamImpl<>(secondQueue, firstQueue);
        second = new MessageStreamImpl<>(firstQueue, secondQueue);
    }

    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    @Nonnull
    @Override
    public MessageStreamImpl first() {
        return first;
    }

    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    @Nonnull
    @Override
    public MessageStreamImpl second() {
        return second;
    }

    @Prefixes({"[MessageStream]"})
    public static final class MessageStreamImpl<M extends Message> implements MessageStream<M> {
        private final MpscAtomicArrayQueue<M> sendQueue;
        private final MpscAtomicArrayQueue<M> receiveQueue;

        public MessageStreamImpl(MpscAtomicArrayQueue<M> sendQueue, MpscAtomicArrayQueue<M> receiveQueue) {
            this.sendQueue = sendQueue;
            this.receiveQueue = receiveQueue;
        }

        @Override
        public void sendMessage(M message) {
            sendQueue.add(message);
        }

        @Nonnull
        @Override
        public M receiveMessage() throws InterruptedException {
            if(Thread.interrupted()) {
                throw new InterruptedException();
            }
            while(receiveQueue.peek() == null) {
                Signaller signaller = new Signaller(0L, 0L, receiveQueue);
                waitForSignaller(signaller);
            }
//            M poll = ;
//            if(poll == null) {
//                poll = receiveMessage();
//            }
            return receiveQueue.poll();
        }

        @Nullable
        @Override
        public M receiveMessage(long timeout, TimeUnit timeUnit) throws InterruptedException {
            if(Thread.interrupted()) {
                return null;
            }
            long nanos = timeUnit.toNanos(timeout);
            if(receiveQueue.peek() == null) {
                long d = System.nanoTime() + nanos;
                Signaller signaller = new Signaller(nanos, d == 0L ? 1L : d, receiveQueue);
                waitForSignaller(signaller);
            }
            return receiveQueue.poll();
        }

        @Override
        public String senderModule() {
            return null;
        }

        private void waitForSignaller(Signaller signaller) {
            if(signaller.thread != null && receiveQueue.peek() == null) {
                try {
                    ForkJoinPool.managedBlock(signaller);
                } catch (InterruptedException e) {
                    Logger.log(e);
                    Thread.currentThread().interrupt();
                }
            }
        }

        private static final class Signaller implements ForkJoinPool.ManagedBlocker {
            long nanos;
            final long deadline;
            final Queue value;
            volatile Thread thread;

            Signaller(long nanos, long deadline, Queue value) {
                this.thread = Thread.currentThread();
                this.nanos = nanos;
                this.deadline = deadline;
                this.value = value;
            }

            public boolean block() {
                if(isReleasable()) {
                    return true;
                } else if(nanos > 0L) {
                    LockSupport.parkNanos(this, nanos);
                } else if(deadline == 0L) {
                    LockSupport.park(this);
                }
                return isReleasable();
            }

            @Override
            public boolean isReleasable() {
                if(deadline != 0L && (nanos <= 0L || (nanos = deadline - System.nanoTime()) <= 0L)) {
                    thread = null;
                    return true;
                }
                return value.peek() != null;
            }
        }
    }
}
