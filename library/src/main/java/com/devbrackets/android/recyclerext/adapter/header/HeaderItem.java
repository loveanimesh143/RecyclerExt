/*
 * Copyright (C) 2016 Brian Wernick
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

package com.devbrackets.android.recyclerext.adapter.header;

import com.devbrackets.android.recyclerext.adapter.HeaderAdapter;

/**
 * An item that represents a Header item as used in the
 * {@link HeaderAdapter}
 */
public class HeaderItem {
    private long id;
    private int adapterPosition;
    private int childCount = 0;

    public HeaderItem(long id, int adapterPosition) {
        this.id = id;
        this.adapterPosition = adapterPosition;
    }

    public long getId() {
        return id;
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }
}
