package datastructures;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import datastructures.RandomAccessTreeList;

public class SortedTreeList<T> implements List<T>, RandomAccess {
	Comparator<T> comparator;
	RandomAccessTreeList list;
	
	public SortedTreeList(Comparator<T> comparator) {
		this.comparator=comparator;
		list=new RandomAccessTreeList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean add(T o) {
		int index = Collections.binarySearch(this, o, comparator);
		if(index<0) index=-index -1;
		//set(index,o);
		if(index>this.size())
			list.add(o);
		else
			list.add(index,o);
		return true;
	}

	public void add(int index, T obj) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends T> arg0) {
		return list.addAll(arg0);
	}

	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		list.clear();
	}

	public boolean contains(Object object) {
		return list.contains(object);
	}

	public boolean containsAll(Collection<?> arg0) {
		return list.containsAll(arg0);
	}

	public boolean equals(Object arg0) {
		return list.equals(arg0);
	}

	public T get(int index) {
		return (T)list.get(index);
	}

	public int hashCode() {
		return list.hashCode();
	}

	public int indexOf(Object object) {
		return list.indexOf(object);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<T> iterator() {
		return list.iterator();
	}

	public int lastIndexOf(Object arg0) {
		return list.lastIndexOf(arg0);
	}

	public ListIterator<T> listIterator() {
		return list.listIterator();
	}

	public ListIterator<T> listIterator(int fromIndex) {
		return list.listIterator(fromIndex);
	}

	public T remove(int index) {
		return (T)list.remove(index);
	}

	public boolean remove(Object arg0) {
		return list.remove(arg0);
	}

	public boolean removeAll(Collection<?> arg0) {
		return list.removeAll(arg0);
	}

	public boolean retainAll(Collection<?> arg0) {
		return list.retainAll(arg0);
	}

	public T set(int index, T obj) {
		return (T)list.set(index, obj);
	}

	public int size() {
		return list.size();
	}

	public List<T> subList(int arg0, int arg1) {
		return list.subList(arg0, arg1);
	}

	public Object[] toArray() {
		return list.toArray();
	}

	public Object[] toArray(Object[] arg0) {
		throw new UnsupportedOperationException();
	}

	public String toString() {
		return list.toString();
	}
	
}
