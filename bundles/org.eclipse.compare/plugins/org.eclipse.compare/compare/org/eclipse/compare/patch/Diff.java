/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import java.util.*;

import org.eclipse.core.runtime.IPath;

import org.eclipse.compare.structuremergeviewer.Differencer;


/* package */ class Diff {
		
	IPath fOldPath, fNewPath;
	long fOldDate, fNewDate;	// if 0: no file
	List fHunks= new ArrayList();
	boolean fIsEnabled= true;
	
 	/* package */ Diff(IPath oldPath, long oldDate, IPath newPath, long newDate) {
		fOldPath= oldPath;
		fOldDate= oldPath == null ? 0 : oldDate;
		fNewPath= newPath;
		fNewDate= newPath == null ? 0 : newDate;	
	}
	
	Hunk[] getHunks() {
		return (Hunk[]) fHunks.toArray((Hunk[]) new Hunk[fHunks.size()]);
	}

	IPath getPath() {
		if (fOldPath != null)
			return fOldPath;
		return fNewPath;
	}
	
	void finish() {
		if (fHunks.size() == 1) {
			Hunk h= (Hunk) fHunks.get(0);
			if (h.fNewLength == 0) {
				fNewDate= 0;
				fNewPath= fOldPath;
			}
		}
	}
	
	/* package */ void add(Hunk hunk) {
		fHunks.add(hunk);
	}
	
	/* package */ int getType() {
		if (fOldDate == 0)
			return Differencer.ADDITION;
		if (fNewDate == 0)
			return Differencer.DELETION;
		return Differencer.CHANGE;
	}
	
	/* package */ String getDescription(int strip) {
		IPath path= fOldPath;
		if (fOldDate == 0)
			path= fNewPath;
		if (strip > 0 && strip < path.segmentCount())
			path= path.removeFirstSegments(strip);
		return path.toOSString();
	}
}

