package util;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Display;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class jna_x11_test {
	public static void main(String[] args) {
	    //creates a new X11 instance
		X11 x11 = X11.INSTANCE;
		//gets the display of the desktop
	    Display display = x11.XOpenDisplay(null);

	    //Gets the root window for the default screen
	    com.sun.jna.platform.unix.X11.Window root = x11.XDefaultRootWindow(display);
	    
	    recurse(x11, display, root, 0);
	}

	private static void recurse(X11 x11, Display display, com.sun.jna.platform.unix.X11.Window root, int depth) {
		
	    X11.WindowByReference windowRef = new X11.WindowByReference();
	    X11.WindowByReference parentRef = new X11.WindowByReference();
	    PointerByReference childrenRef = new PointerByReference();
	    IntByReference childCountRef = new IntByReference();
	    
	    x11.XQueryTree(display, root, windowRef, parentRef, childrenRef, childCountRef);
	    if (childrenRef.getValue() == null) {
	        return;
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
	        

	        if(attributes.map_state == 2 && (attributes.x > 0 || attributes.y > 0) && name.value != null) {
	        	System.out.println(String.join("", java.util.Collections.nCopies(depth, "  ")) + name.value);
	        	System.out.println(attributes.width + " " + attributes.height + " " + attributes.x + " " + attributes.y + "\n");
	        }

	        x11.XFree(name.getPointer());

	        recurse(x11, display, window, depth + 1);
	    }
	}
}
