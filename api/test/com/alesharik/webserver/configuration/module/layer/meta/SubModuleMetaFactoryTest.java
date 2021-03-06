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

package com.alesharik.webserver.configuration.module.layer.meta;

import com.alesharik.webserver.base.bean.Bean;
import com.alesharik.webserver.configuration.module.Shutdown;
import com.alesharik.webserver.configuration.module.ShutdownNow;
import com.alesharik.webserver.configuration.module.Start;
import com.alesharik.webserver.configuration.module.layer.SubModule;
import com.alesharik.webserver.configuration.module.meta.MetaInvokeException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.alesharik.webserver.api.TestUtils.assertUtilityClass;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SubModuleMetaFactoryTest {
    private static BiConsumer<SubModuleAdapter, Object> processor;

    @BeforeClass
    public static void createTest() {
        SubModuleMetaFactory.listenClass(SubModuleProcessorImpl.class);
    }

    @Before
    public void setUp() throws Exception {
        //noinspection unchecked
        processor = mock(BiConsumer.class);
    }

    @Test
    public void create() {
        val mock = mock(SubModuleAdapter.class);

        SubModuleImpl o = new SubModuleImpl(mock);
        SubModuleAdapter adapter = SubModuleMetaFactory.create(o);

        assertEquals("test", adapter.getName());
        assertNotNull(adapter.getCustomData());

        verify(processor).accept(adapter, o);

        assertFalse(adapter.isRunning());

        adapter.start();
        verify(mock, times(2)).start();
        assertTrue(adapter.isRunning());

        adapter.start();
        verify(mock, times(2)).start();//Nothing happens
        assertTrue(adapter.isRunning());

        adapter.shutdown();
        verify(mock, times(1)).shutdown();
        assertFalse(adapter.isRunning());

        adapter.shutdown();
        verify(mock, times(1)).shutdown();//Nothing happens
        assertFalse(adapter.isRunning());

        adapter.start();
        assertTrue(adapter.isRunning());

        adapter.shutdownNow();
        verify(mock, times(1)).shutdownNow();
        assertFalse(adapter.isRunning());

        adapter.shutdownNow();
        verify(mock, times(1)).shutdownNow();//Nothing happens
        assertFalse(adapter.isRunning());
    }

    @Test(expected = IllegalArgumentException.class)
    public void tryToCreateNotASubModule() {
        SubModuleMetaFactory.create(new Object());
        fail();
    }

    @Test
    public void errorInProcessor() {
        doThrow(new IllegalMonitorStateException()).when(processor).accept(any(), any());
        SubModuleAdapter adapter = SubModuleMetaFactory.create(new SubModuleImpl(mock(SubModuleAdapter.class)));

        assertFalse(adapter.isRunning());
        adapter.start();
        assertTrue(adapter.isRunning());
        adapter.shutdown();
        assertFalse(adapter.isRunning());
    }

    @Test
    public void addBeanProcessor() {
        assertEquals(1, SubModuleMetaFactory.processors.size());

        SubModuleMetaFactory.listenClass(BeanProcessor.class);
        assertEquals(2, SubModuleMetaFactory.processors.size());

        SubModuleMetaFactory.listenClass(BeanProcessor.class);
        assertEquals(2, SubModuleMetaFactory.processors.size());

        SubModuleMetaFactory.listenClass(SubModuleProcessorImpl.class);
        assertEquals(2, SubModuleMetaFactory.processors.size());
    }

    @Test(expected = LinkageError.class)
    public void errorOnInvoke() {
        SubModuleAdapter adapter = mock(SubModuleAdapter.class);
        doThrow(new LinkageError()).when(adapter).start();

        SubModuleMetaFactory.create(new SubModuleImpl(adapter)).start();
    }

    @Test(expected = MetaInvokeException.class)
    public void exceptionOnInvoke() {
        SubModuleAdapter adapter = mock(SubModuleAdapter.class);
        doThrow(new NullPointerException()).when(adapter).start();

        SubModuleMetaFactory.create(new SubModuleImpl(adapter)).start();
    }

    @Test
    public void testUtility() {
        assertUtilityClass(SubModuleMetaFactory.class);
    }

    private static final class SubModuleProcessorImpl implements SubModuleProcessor {

        @Override
        public void processSubModule(SubModuleAdapter adapter, Object subModule) {
            processor.accept(adapter, subModule);
        }
    }

    @Bean
    private static final class BeanProcessor implements SubModuleProcessor {

        @Override
        public void processSubModule(SubModuleAdapter adapter, Object subModule) {

        }
    }

    @RequiredArgsConstructor
    @SubModule("test")
    private static final class SubModuleImpl {
        private final SubModuleAdapter mock;

        @Start
        private void start() {
            mock.start();
        }

        @Start
        private void start1() {
            mock.start();
        }

        @Shutdown
        private void shutdown() {
            mock.shutdown();
        }

        @ShutdownNow
        private void shutdownNow() {
            mock.shutdownNow();
        }
    }
}