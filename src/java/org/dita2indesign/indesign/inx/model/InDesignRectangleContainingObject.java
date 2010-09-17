/**
 * Copyright 2009 DITA2InDesign project (dita2indesign.sourceforge.net)  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at     http://www.apache.org/licenses/LICENSE-2.0  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.  n nLicensed under the Apache License, Version 2.0 (the "License"); nyou may not use this file except in compliance with the License. nYou may obtain a copy of the License at n n   http://www.apache.org/licenses/LICENSE-2.0 n nUnless required by applicable law or agreed to in writing, software ndistributed under the License is distributed on an "AS IS" BASIS, nWITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. nSee the License for the specific language governing permissions and nlimitations under the License.   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at     http://www.apache.org/licenses/LICENSE-2.0  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License. 
 */
package org.dita2indesign.indesign.inx.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dita2indesign.indesign.inx.visitors.InDesignDocumentVisitor;
import org.w3c.dom.Element;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;



/**
 *
 */
public abstract class InDesignRectangleContainingObject extends InDesignGeometryHavingObject {

	private static Logger logger = Logger.getLogger(InDesignRectangleContainingObject.class);

	protected Map<String, Rectangle> rectangles = new HashMap<String, Rectangle>();
	protected Map<String, TextFrame> frames = new HashMap<String, TextFrame>();
	private List<Group> groups = new ArrayList<Group>();

	/**
	 * @throws Exception 
	 * 
	 */
	public InDesignRectangleContainingObject() throws Exception {
		super();
	}

	/**
	 * @param dataSource
	 * @throws Exception 
	 */
	public InDesignRectangleContainingObject(Element dataSource) throws Exception {
		super();
		this.loadObject(dataSource);
	}

	/* (non-Javadoc)
	 * @see org.dita2indesign.indesign.inx.model.AbstractInDesignObject#loadObject(org.dita2indesign.indesign.inx.model.InDesignObject)
	 */
	@Override
	public void loadObject(InDesignObject sourceObj) throws Exception {
		logger.debug("loadObject(): loading from source object " + sourceObj.getId() + ", type " + sourceObj.getClass().getSimpleName());
		String id = this.getId(); // If ID has been assigned, we don't want to inherent
		Element sourceElem = sourceObj.getDataSourceElement();
		if (sourceElem != null) {
			Element newDataSource = (Element) sourceElem.cloneNode(false); // Just get the node, not it's children
			this.setDataSource(newDataSource);
			// If we're a new clone, use already-assigned ID, not ID from data source.
			if (id != null)
				this.setId(id);
			// Now iterate over the children of the source object and load them:
			for (InDesignComponent childObj : sourceObj.getChildren()) {
				if (childObj instanceof InDesignObject) {
					InDesignObject newObj = this.getDocument().clone((InDesignObject)childObj);
					this.addChild(newObj);
				} else {
					this.addChild(childObj);
				}
			}
		} else {
			// Handle cloning of child components with no data source element.
			throw new NotImplementedException();
		}
	}

	public void loadObject(Element dataSource) throws Exception {
		logger.debug("loadObject(): loading from data source element \"" + dataSource.getNodeName() + "\"");
		super.loadObject(dataSource);
		for (InDesignComponent child : this.getChildren()) {
			if (child instanceof TextFrame) {
				this.frames.put(((TextFrame) child).getId(), (TextFrame)child);
			} 
			
			// rectangles includes text frames
			if (child instanceof Rectangle) {
				this.rectangles.put(((Rectangle) child).getId(), (Rectangle)child);				
			} else if (child instanceof Group) {
				this.groups.add((Group)child);				
			}
		}

	}

	/**
	 * @param child
	 * @throws Exception 
	 */
	protected void newGroup(Element dataSource) throws Exception {
		Group group = this.getDocument().newGroup(dataSource);
		this.groups.add(group);
		this.addChild(group);
		
	}

	/**
	 * @param dataSource
	 * @throws Exception 
	 */
	public void newRectangle(Element dataSource) throws Exception {		
		Rectangle rect = this.getDocument().newRectangle(dataSource);
		this.rectangles.put(rect.getId(), rect);
		this.addChild(rect);
	}

	/**
	 * @return All rectangles (including text frames)
	 */
	public List<Rectangle> getRectangles() {
		return new ArrayList<Rectangle>(this.rectangles.values());
	}

	/**
	 * @param dataSource
	 * @throws Exception 
	 */
	public InDesignObject newFrame(Element dataSource) throws Exception {
		TextFrame frame = this.getDocument().newFrame(dataSource);
		this.frames.put(frame.getId(), frame);
		this.rectangles.put(frame.getId(), frame);
		this.addChild(frame);
		return frame;
		
	}

	/**
	 * @return List of all the frames in the container.
	 */
	public List<TextFrame> getAllFrames() {
		return new ArrayList<TextFrame>(this.frames.values());
	}

	/**
	 * @param rect
	 */
	public void addRectangle(Rectangle rect) {
		logger.debug("addRectang(): rect=" + rect);
		this.rectangles.put(rect.getId(), rect);
		if (rect instanceof TextFrame)
			this.frames.put(rect.getId(), (TextFrame)rect);
		this.addChild(rect);
	}

	/**
	 * @param visitor
	 * @throws Exception 
	 */
	public void accept(InDesignDocumentVisitor visitor) throws Exception {
		visitor.visit(this);
	}



}
