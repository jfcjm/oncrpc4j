package fr.univjfc.si.architecture.tests;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;


import spoon.Launcher;
import spoon.processing.AbstractProcessor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.chain.CtQuery;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

@Ignore
public class TestLayering {

   
    public class VoidProessor extends AbstractProcessor {

        @Override
        public void process(CtElement element) {
            // TODO Auto-generated method stub

        }

    }

    @Test
    public void test() {
        Launcher launcher = new Launcher();
        launcher.addInputResource("src/main/java");
        launcher.buildModel();
        CtModel model = launcher.getModel();
        
         Collection<CtPackage> packs = model.getAllPackages();
         for( CtPackage p : packs){
             AbstractProcessor processor = getProcessor (p.getQualifiedName());
             System.out.println("=========================== " + p.getQualifiedName());
             Set<CtType<?>> types = p.getTypes();
             types.forEach(ref -> processor.process(ref));
         }
        fail("Not yet implemented");
    }

    private  AbstractProcessor getProcessor(String simpleName) {
        HashMap<String, AbstractProcessor> processors = new HashMap<String,AbstractProcessor>();
        processors.put("org.dcache.xdr.model.itf",  new  AbstractProcessor<CtType> (){

            @Override
            public void process(CtType element) {
                System.out.println(element.getQualifiedName());
                if (null == element.getReferencedTypes()) return;
                element.getReferencedTypes().forEach(t -> processTopLayer(element,t));
                
            }
            
        });
        assertEquals(1,processors.keySet().size());
        AbstractProcessor result = processors.get(simpleName);
        System.out.println(result);
        if(result == null){
            result =  new VoidProessor();
        }
        return result;
    }

    private void checkIntraItfLayer(CtPackage pack) {
        CtQuery refs = pack.filterChildren((CtTypeReference<?> ref) -> true);
        
        refs.forEach( ref -> System.out.println(ref));
    }

    private void processTopLayer(CtType element, CtTypeReference<?> t) {
        if (isLocal(t)){
            System.out.println(t.isPrimitive() ? "" : "***********" + t.getPackage().getSimpleName());
            System.out.println("Model " + isModelLayer(t));
            System.out.println("Itf " + isTopItfLayer(t));
            System.out.println("Abstract " + isAbstractLayer(t));
            if (!isModelLayer(t)){
                System.out.println("//////////////////// uses " + t.getSimpleName());
            }
        }
        }
    
    private boolean isTopItfLayer(CtTypeReference<?> t) {
        return t.getQualifiedName().startsWith("org.dcache.xdr.model.itf");
    }
    
    private boolean isModelLayer(CtTypeReference<?> t) {
        return t.getQualifiedName().startsWith("org.dcache.xdr.model");
    }
    
    private boolean isAbstractLayer(CtTypeReference<?> t) {
        return t.getQualifiedName().startsWith("org.dcache.xdr.model.root");
    }

    private boolean isLocal(CtTypeReference<?> t) {
        
        boolean result = t.isPrimitive() ;
        if (! result){
            result= t.getQualifiedName().startsWith("org.dcache.xdr");
            
        }
        return result;
    }

}
