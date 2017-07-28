package util;

import java.util.ArrayList;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Display;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class X11InfoGatherer {
	
	static X11InfoGatherer gatherer;
	X11 x11;
	Display display;
	com.sun.jna.platform.unix.X11.Window root;
    long parentWindowId = -1;
	
    public static X11InfoGatherer getX11InfoGatherer() {
    	if(gatherer != null) {
    		return gatherer;
    	} else {
    		return new X11InfoGatherer();
    	}
    }
    
	/**
	 * Creates references to a new X11 instance, the computer's display, and the root window.
	 */
	private X11InfoGatherer() {
		x11 = X11.INSTANCE;
		display = x11.XOpenDisplay(null);
		root = x11.XDefaultRootWindow(display);
	}
	
	/**
	 * Gets an arraylist of a WindowInfo instance for each open window.
	 * 
	 * @return An ArrayList containing one WindowInfo for each open window.
	 */
	public ArrayList<WindowInfo> getAllWindows() {
		return getAllWindows(this.root, 0);
	}
	
	/**
	 * Recursively gets an ArrayList of one WindowInfo for each open window within the root window.
	 * 
	 * @param root The root window to recursively retrieve open windows from.
	 * @param depth How many windows deep the recursive search has gone.
	 * @return The ArrayList containing one WindowInfo for each open window.
	 */
	private ArrayList<WindowInfo> getAllWindows(com.sun.jna.platform.unix.X11.Window root, int depth) {
		ArrayList<WindowInfo> windowList = new ArrayList<WindowInfo>();
		
		X11.WindowByReference windowRef = new X11.WindowByReference();
	    X11.WindowByReference parentRef = new X11.WindowByReference();
	    PointerByReference childrenRef = new PointerByReference();
	    IntByReference childCountRef = new IntByReference();
	    
	    x11.XQueryTree(display, root, windowRef, parentRef, childrenRef, childCountRef);
	    if (childrenRef.getValue() == null) {
	        return new ArrayList<WindowInfo>();
	    }

	    long[] ids;

	    if (Native.LONG_SIZE == Long.BYTES) {
	        ids = childrenRef.getValue().getLongArray(0, childCountRef.getValue());
	    } else if (Native.LONG_SIZE == Integer.BYTES) {
	        int[] intIds = childrenRef.getValue().getIntArray(0, childCountRef.getValue());
	        ids = new long[intIds.length];
	        for (int i = 0; i < intIds.length; i++) {
	            ids[i] = intIds[i];
	        }
	    } else {
	        throw new IllegalStateException("Unexpected size for Native.LONG_SIZE" + Native.LONG_SIZE);
	    }

	    for (long id : ids) {
	        if (id == 0) {
	            continue;
	        }
	        com.sun.jna.platform.unix.X11.Window window = new com.sun.jna.platform.unix.X11.Window(id);
	        X11.XTextProperty name = new X11.XTextProperty();
	        x11.XGetWMName(display, window, name);
	        X11.XWindowAttributes attributes = new X11.XWindowAttributes();
	        x11.XGetWindowAttributes(display, window, attributes);
	        

	        // If the window has no title, it may be the parent of a window that has a title, and contain important position information.
	        // Save the window ID, and pair it with the next ID if that window has a title. Windows with no titleless parent use their own id twice.
	        if(attributes.map_state == 2 && (attributes.x > 0 || attributes.y > 0)) {
	        	if(name.value == null) {
	        		parentWindowId = id;
	        	} else {
	        		if(parentWindowId == -1) {
	        			windowList.add(new WindowInfo(id, id, name.value));
	        		} else {
	        			windowList.add(new WindowInfo(parentWindowId, id, name.value));
	        			parentWindowId = -1;
	        		}
	        	}
	        }

	        
	        ArrayList<WindowInfo> recursiveWindowList = getAllWindows(window, depth + 1);
	        windowList.addAll(recursiveWindowList);
	    }
	    return windowList;
	}
	
	/**
	 * Returns the title of the window associated with a given id.
	 * 
	 * @param id The window id of the window.
	 * @return The title of the window associated with a given id.
	 */
	public String getWindowName(long id) {
		com.sun.jna.platform.unix.X11.Window window = new com.sun.jna.platform.unix.X11.Window(id);
		X11.XTextProperty name = new X11.XTextProperty();
		x11.XGetWMName(display, window, name);
		
		return name.value;
	}
	
	/**
	 * Returns an array containing the width, height, x, and y attributes of a window.
	 * 
	 * @param id The window id of the window to retrieve the attributes from.
	 * @return An array containing the width, height, x, and y attributes of a window, as ints.
	 */
	public int[] getWindowAttributes(long id) {
		com.sun.jna.platform.unix.X11.Window window = new com.sun.jna.platform.unix.X11.Window(id);
		X11.XWindowAttributes attributes = new X11.XWindowAttributes();
        x11.XGetWindowAttributes(display, window, attributes);
        int[] dimensions = {attributes.width, attributes.height, attributes.x, attributes.y};
        return dimensions;
	}
}
