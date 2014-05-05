package demo.command.base;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import demo.infrastructure.DateUtil;

/**
 * ���𴴽���������״̬�¼��������Ĺ���
 * 
 * ��״̬�¼���������ʵ�������һ��ʱ���ں��п��ܲ��뵽�¼��Ĵ����У������¼��ķ����� Ҳ���¼��������ߣ�����ʵ������Ӧ���ִ��������ڴ���һ��ʱ�䡣
 * 
 * ��״̬�¼���������ʵ������һ������Ҫװ���Լ���Ҫ�����ݣ�Ϊ�˴����¼�����Ҫ�Ļ�����
 * Ϣ������һ����Ϊ������Լ���Ϊ�¼����������нڵ��ְ����Ҫ�����¼��ط�
 * 
 * @author user
 * 
 */
public class StateHandlerFactory {

	private static StateHandlerFactory factory = new StateHandlerFactory();

	private Map<StateHandler, Date> handlers = new Hashtable<StateHandler, Date>();

	private Timer clearTimer;

	private final long keepDate = 10000;

	private StateHandlerFactory() {
		this.clearTimer = new Timer();
		this.clearTimer.schedule(new ClearStateHandler(), 1000, 2000);
	}

	public static StateHandlerFactory getInstance() {
		return factory;
	}

	public <T extends StateHandler> T getStateHandler(Class<T> c,
			String id) throws Exception {
		T handler = findStateHandler(c, new Object[] { id });
		if (handler != null) {
			return handler;
		} else {
			Constructor<T> constructor = c
					.getConstructor(new Class[] { String.class });

			handler = constructor.newInstance(id);

			handler.load();
			handler.replay();

			handlers.put(handler, DateUtil.getSysDate());

			return handler;
		}
	}

	public <T extends StateHandler> T createStateHandler(Class<T> c,
			Object[] args) throws Exception {

		T handler = findStateHandler(c, args);
		if (handler != null) {
			return handler;
		} else {
			if (args == null) {
				handler = c.newInstance();
			} else {
				Class[] argTypes = new Class[args.length];
				for (int i = 0; i < args.length; i++) {
					argTypes[i] = args[i].getClass();
				}
				Constructor<T> constructor = c.getConstructor(argTypes);

				handler = constructor.newInstance(args);
			}
			handlers.put(handler, DateUtil.getSysDate());

			return handler;
		}
	}

	private <T extends StateHandler> T findStateHandler(Class<T> c,
			Object[] args) throws Exception {
		if (args == null || args.length == 0) {
			throw new IllegalArgumentException("args is null");
		}

		for (StateHandler handler : handlers.keySet()) {
			if (handler.getClass().equals(c) && handler.getId().equals(args[0])) {
				handlers.put(handler, DateUtil.getSysDate());
				return (T) handler;
			}
		}

		return null;
	}

	public void destory() throws InterruptedException {
		this.clearTimer.cancel();
	}

	class ClearStateHandler extends TimerTask {
		@Override
		public void run() {
			Date clearDate = new Date(DateUtil.getSysDate().getTime()
					- keepDate);
			Iterator<StateHandler> it = handlers.keySet().iterator();
			StateHandler handler;
			while (it.hasNext()) {
				handler = it.next();
				if (handlers.get(handler).compareTo(clearDate) < 0) {
					try {
						handler.destory();
						handler.save();
						it.remove();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
