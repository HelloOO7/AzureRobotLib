package azure.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EqualsMap<K, V> implements Map<K, V>{

	private List<K> keys = new ArrayList<>();
	private List<V> values = new ArrayList<>();

	@Override
	public void clear() {
		keys.clear();
		values.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return keys.contains(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return values.contains(value);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> set = new HashSet<>();
		for (int i = 0; i < keys.size(); i++) {
			set.add(new Entry(keys.get(i)));
		}
		return set;
	}

	public class Entry implements Map.Entry {

		public Entry(K key){
			this.key = key;
		}

		private K key;

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return (V)get(key);
		}

		@Override
		public V setValue(Object value) {
			return put(key, (V) value);
		}

	}

	@Override
	public V get(Object key) {
		return values.get(keys.indexOf(key));
	}

	@Override
	public boolean isEmpty() {
		return keys.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return new HashSet<>(keys);
	}

	@Override
	public V put(K key, V value) {
		int idx = keys.indexOf(key);
		if (idx == -1) {
			keys.add(key);
			values.add(value);
			return value;
		}
		else {
			return values.set(idx, value);
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> t) {

	}

	@Override
	public V remove(Object key) {
		int index = keys.indexOf(key);
		keys.remove(index);
		return values.remove(index);
	}

	@Override
	public int size() {
		return keys.size();
	}

	@Override
	public Collection<V> values() {
		return values();
	}

}
