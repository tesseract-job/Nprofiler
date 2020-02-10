package org.nickle.nprofiler.perf.hat.query;

import com.sun.tools.hat.internal.model.JavaHeapObject;
import com.sun.tools.hat.internal.model.ReferenceChain;
import com.sun.tools.hat.internal.model.Root;
import com.sun.tools.hat.internal.util.ArraySorter;
import com.sun.tools.hat.internal.util.Comparer;
import org.nickle.nprofiler.bean.InstanceInfo;
import org.nickle.nprofiler.bean.RootsInfo;
import org.nickle.nprofiler.exception.NprofilerException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 根集合查询
 * @author: 1336942608@qq.com
 * @version:
 * @date: 2020年01月17 22时59分
 */
public class NRootsQuery extends NQueryHandler{
    private boolean includeWeak;

    public NRootsQuery(boolean var1) {
        this.includeWeak = var1;
    }

    @Override
    public Object run() {
        long id = parseHex(query);
        JavaHeapObject heapObject = this.snapshot.findThing(id);
        if (heapObject == null) {
            throw new NprofilerException("无法站找到根集合");
        } else {
            RootsInfo rootsInfo = new RootsInfo();
            if (this.includeWeak) {
                rootsInfo.setIncludeWeak((byte)1);
            }
            // 引用链
            ReferenceChain[] chains = this.snapshot.rootsetReferencesTo(heapObject, this.includeWeak);
            // sort
            ArraySorter.sort(chains, new Comparer() {
                @Override
                public int compare(Object var1, Object var2) {
                    ReferenceChain var3 = (ReferenceChain)var1;
                    ReferenceChain var4 = (ReferenceChain)var2;
                    Root var5 = var3.getObj().getRoot();
                    Root var6 = var4.getObj().getRoot();
                    int var7 = var5.getType() - var6.getType();
                    return var7 != 0 ? -var7 : var3.getDepth() - var4.getDepth();
                }
            });

            InstanceInfo instanceInfo = this.parseThing(heapObject);
            rootsInfo.setRefInstance(instanceInfo);
            int var5 = 0;

            List<RootsInfo.RootInfo> rootInfoList = new ArrayList<>(chains.length);
            // cycle
            for(int i = 0; i < chains.length; ++i) {
                ReferenceChain chain = chains[i];
                // root
                Root root = chain.getObj().getRoot();
                RootsInfo.RootInfo rootInfo = parseRoot(root);
                if (root.getType() != var5) {
                    var5 = root.getType();
                    rootInfo.setReferenceTypeName(root.getTypeName());
                }

                // object
                if (root.getReferer() != null) {
                    JavaHeapObject referer = root.getReferer();
                    rootInfo.setFromObject(referer.toString());
                    rootInfo.setFromObjectId(referer.getId());
                }

                List<InstanceInfo> referenceChains = new LinkedList<>();
                // stack
                while(chain != null) {
                    ReferenceChain chainNext = chain.getNext();
                    JavaHeapObject obj = chain.getObj();
                    InstanceInfo instance = this.parseThing(obj);
                    if (chainNext != null) {
                        instance.setReferenceToDesc(obj.describeReferenceTo(chainNext.getObj(), this.snapshot));
                    }
                    referenceChains.add(instance);
                    chain = chainNext;
                }
                rootInfo.setReferenceChains(referenceChains);
                rootInfoList.add(rootInfo);
            }
            rootsInfo.setRootInfoList(rootInfoList);
            return rootsInfo;
        }
    }
}
