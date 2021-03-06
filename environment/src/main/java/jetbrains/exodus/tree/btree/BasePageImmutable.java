/**
 * Copyright 2010 - 2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.exodus.tree.btree;

import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.ByteIterator;
import jetbrains.exodus.ExodusException;
import jetbrains.exodus.bindings.LongBinding;
import jetbrains.exodus.core.dataStructures.LongObjectCacheBase;
import jetbrains.exodus.log.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class BasePageImmutable extends BasePage {

    @NotNull
    protected final ByteIterableWithAddress data;
    private long dataAddress;
    byte keyAddressLen;

    /**
     * Create empty page
     *
     * @param tree tree which the page belongs to
     */
    BasePageImmutable(@NotNull BTreeBase tree) {
        super(tree);
        data = ByteIterableWithAddress.EMPTY;
        size = 0;
        dataAddress = Loggable.NULL_ADDRESS;
    }

    /**
     * Create page and load size and key address length
     *
     * @param tree tree which the page belongs to
     * @param data binary data to load the page from.
     */
    BasePageImmutable(@NotNull BTreeBase tree, @NotNull final ByteIterableWithAddress data) {
        super(tree);
        this.data = data;
        final ByteIteratorWithAddress it = data.iterator();
        size = CompressedUnsignedLongByteIterable.getInt(it) >> 1;
        init(it);
    }

    /**
     * Create page of specified size and load key address length
     *
     * @param tree tree which the page belongs to
     * @param data source iterator
     * @param size computed size
     */
    BasePageImmutable(@NotNull BTreeBase tree, @NotNull final ByteIterableWithAddress data, int size) {
        super(tree);
        this.data = data;
        this.size = size;
        init(data.iterator());
    }

    private void init(@NotNull final ByteIteratorWithAddress itr) {
        if (size > 0) {
            final int next = itr.next();
            dataAddress = itr.getAddress();
            loadAddressLengths(next);
        } else {
            dataAddress = itr.getAddress();
        }
    }

    @Override
    protected long getDataAddress() {
        return dataAddress;
    }

    ByteIterator getDataIterator(final int offset) {
        return dataAddress == Loggable.NULL_ADDRESS ?
                ByteIterable.EMPTY_ITERATOR : data.iterator((int) (dataAddress - data.getDataAddress() + offset));
    }

    protected void loadAddressLengths(final int length) {
        checkAddressLength(keyAddressLen = (byte) length);
    }

    static void checkAddressLength(byte addressLen) {
        if (addressLen < 0 || addressLen > 8) {
            throw new ExodusException("Invalid length of address: " + addressLen);
        }
    }

    @Override
    protected long getKeyAddress(final int index) {
        return getDataIterator(index * keyAddressLen).nextLong(keyAddressLen);
    }

    @Override
    @NotNull
    public BaseLeafNode getKey(final int index) {
        return getTree().loadLeaf(getKeyAddress(index), getTreeNodesCache());
    }

    @Override
    protected boolean isMutable() {
        return false;
    }

    @Override
    protected SearchRes binarySearch(final ByteIterable key) {
        return binarySearch(key, 0);
    }

    @Override
    protected SearchRes binarySearch(final ByteIterable key, final int low) {
        if (dataAddress == Loggable.NULL_ADDRESS) {
            return SearchRes.NOT_FOUND;
        }
        final LongObjectCacheBase treeNodesCache = getTreeNodesCache();
        final SearchRes result = new SearchRes();
        result.index = (short) ByteIterableWithAddress.binarySearch(
                new IByteIterableComparator() {
                    @Override
                    public int compare(final long leftAddress, @NotNull final ByteIterable right) {
                        return (result.key = getTree().loadLeaf(leftAddress, treeNodesCache)).compareKeyTo(right);
                    }
                }, key, low, size - 1, keyAddressLen, getTree().log, dataAddress);
        if (result.index < 0) {
            result.key = null;
        }
        return result;
    }

    @Nullable
    protected LongObjectCacheBase getTreeNodesCache() {
        return null;
    }

    static void doReclaim(BTreeReclaimTraverser context) {
        final BasePageMutable node = context.currentNode.getMutableCopy(context.mainTree);
        context.wasReclaim = true;
        context.setPage(node);
        context.popAndMutate();
    }
}
