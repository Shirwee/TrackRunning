package com.shirwee.trackrunning;

import android.support.annotation.NonNull;

/**
 * @author shirwee
 *
 */
public class Item
        implements Comparable<Item>
{
	private int    id;
	private int    current;
	private int    target;

	public Item(int id, int current, int target) {
		this.id = id;
		this.current = current;
		this.target = target;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}

	@Override
	public int compareTo(@NonNull Item o) {
		//按照分数排序
		int num1= this.getTarget()-o.getTarget();
		////如果分数相同，按名字排序
//		int num2=num1==0?(this.getName().compareTo(o.getName())):num1;
		return num1;
	}
}
