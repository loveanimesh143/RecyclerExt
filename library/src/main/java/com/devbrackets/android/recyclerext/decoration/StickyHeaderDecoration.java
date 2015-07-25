/*
 * Copyright (C) 2015 Brian Wernick
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.devbrackets.android.recyclerext.decoration;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.devbrackets.android.recyclerext.R;
import com.devbrackets.android.recyclerext.adapter.RecyclerHeaderAdapter;

/**
 * A RecyclerView Decoration that allows for Header views from
 * the {@link RecyclerHeaderAdapter} to be persisted when they
 * reach the top of the RecyclerView's frame.
 */
public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {

    public enum LayoutOrientation {
        VERTICAL,
        HORIZONTAL
    }

    @Nullable
    private Bitmap stickyHeader;
    private int stickyStart = 0;
    private RecyclerView parent;
    private LayoutOrientation orientation = LayoutOrientation.VERTICAL;

    public StickyHeaderDecoration(RecyclerView parent) {
        this.parent = parent;
        parent.addOnScrollListener(new StickyViewScrollListener());
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (stickyHeader != null) {
            int x = orientation == LayoutOrientation.HORIZONTAL ? stickyStart : 0;
            int y = orientation == LayoutOrientation.HORIZONTAL ? 0 : stickyStart;
            c.drawBitmap(stickyHeader, x,y, null);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
    }

    /**
     * Sets the orientation of the current layout
     *
     * @param orientation The layouts orientation
     */
    public void setOrientation(LayoutOrientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Retrieves the current orientation to use for edgeScrolling and position calculations.
     *
     * @return The current orientation [default: {@link LayoutOrientation#VERTICAL}]
     */
    public LayoutOrientation getOrientation() {
        return orientation;
    }

    /**
     * Generates the Bitmap that will be used to represent the view stuck at the top of the
     * parent RecyclerView.
     *
     * @param view The view to create the drag bitmap from
     * @return The bitmap representing the drag view
     */
    private Bitmap createStickyViewBitmap(View view) {
        Rect stickyViewBounds = new Rect(0, 0, view.getRight() - view.getLeft(), view.getBottom() - view.getTop());

        Bitmap bitmap = Bitmap.createBitmap(stickyViewBounds.width(), stickyViewBounds.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }




    //TODO: this doesn't work correctly when scrolling towards the start of the list (header doesn't appear until hitting the view location)
    private class StickyViewScrollListener extends RecyclerView.OnScrollListener {
        private long currentStickyId = Long.MIN_VALUE;
        private int[] windowLocation = new int[2];
        private int parentStart = Integer.MIN_VALUE;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            View nextHeader = findNextHeader(recyclerView);
            if (nextHeader == null) {
                return;
            }

            //If the next header is different than the current one, perform the swap
            Long headerId = (Long)nextHeader.getTag(R.id.sticky_view_header_id);
            if (headerId != null && headerId != currentStickyId) {
                performStickyHeaderSwap(nextHeader, headerId);
            }
        }

        private void performStickyHeaderSwap(View nextHeader, long headerId) {
            int nextHeaderStart = orientation == LayoutOrientation.HORIZONTAL ? windowLocation[0] : windowLocation[1];
            Log.d("StickyDecoration", "nextHeaderStart: " + nextHeaderStart + ", parentStart: " + parentStart);

            int trueStart = nextHeaderStart - parentStart;
            if (stickyHeader != null && trueStart > 0) {
                stickyStart = trueStart - (orientation == LayoutOrientation.HORIZONTAL ? stickyHeader.getWidth() : stickyHeader.getHeight());
            } else {
                stickyStart = 0;
                currentStickyId = headerId;
                stickyHeader = createStickyViewBitmap(nextHeader);
            }
        }

        @Nullable
        private View findNextHeader(RecyclerView recyclerView) {
            int attachedViewCount = recyclerView.getLayoutManager().getChildCount();
            if (attachedViewCount <= 0) {
                return null;
            }

            //Make sure we have the start of the RecyclerView stored
            if (parentStart == Integer.MIN_VALUE) {
                parent.getLocationInWindow(windowLocation);
                parentStart = orientation == LayoutOrientation.HORIZONTAL ? windowLocation[0] : windowLocation[1];
            }

            //Determines the max start position to look for the next sticky header
            int maxStartPosition = parentStart;
            if (stickyHeader != null) {
                if (orientation == LayoutOrientation.HORIZONTAL) {
                    maxStartPosition += stickyHeader.getWidth() + 1;
                } else {
                    maxStartPosition += stickyHeader.getHeight() + 1;
                }
            }

            //Attempts to find the first header
            for (int viewIndex = 0; viewIndex < attachedViewCount; viewIndex++) {
                View view = recyclerView.getLayoutManager().getChildAt(viewIndex);
                view.getLocationInWindow(windowLocation);

                //If the start location is greater than the max, we don't have a header to worry about
                int startLoc = orientation == LayoutOrientation.HORIZONTAL ? windowLocation[0] : windowLocation[1];
                if (startLoc > maxStartPosition) {
                    return null;
                }

                //Determine if the view is a header to return
                Integer type = (Integer)view.getTag(R.id.sticky_view_type_tag);
                if (type != null && type == RecyclerHeaderAdapter.VIEW_TYPE_HEADER) {
                    return view;
                }
            }

            return null;
        }
    }
}
