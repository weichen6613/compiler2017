package com.mercy.compiler.Entity;

import com.mercy.Option;
import com.mercy.compiler.AST.BlockNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.BackEnd.BasicBlock;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Label;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.INS.Operand.Register;
import com.mercy.compiler.IR.IR;
import com.mercy.compiler.Type.FunctionType;
import com.mercy.compiler.Type.Type;

import java.util.*;

/**
 * Created by mercy on 17-3-20.
 */
public class FunctionEntity extends Entity {
    private Type returnType;
    private List<ParameterEntity> params;
    private BlockNode body;
    private Scope scope;
    private boolean isConstructor = false;
    
    private boolean canbeInlined = false;
    private List<FunctionEntity> calls = new LinkedList<>();
    
    private com.mercy.compiler.IR.Label beginLabelIR, endLabelIR;
    private com.mercy.compiler.INS.Label beginLabelINS, endLabelINS;
    private List<IR> irs;
    private List<Instruction> ins;
    private List<BasicBlock> bbs;
    private List<Reference> tmpStack;
    private int frameSize;

    private List<Register> regUsed = new LinkedList<>();
    private Set<Reference> allReference = new HashSet<>();

    private String asmName;

    public FunctionEntity(Location loc, Type returnType, String name, List<ParameterEntity> params, BlockNode body) {
        super(loc, new FunctionType(name), name);
        this.params = params;
        this.body = body;
        this.returnType = returnType;
        ((FunctionType)this.type).setEntity(this);
        this.asmName = null;
    }

    public ParameterEntity addThisPointer(Location loc, ClassEntity entity) {
        ParameterEntity thisPointer = new ParameterEntity(entity.location(), entity.type(), "this");
        params.add(0, thisPointer);
        return thisPointer;
    }

    // check whether can be inlined
    Map<FunctionEntity, Boolean> visited;
    public void checkInlinable() {
        if (name.equals("main")) {
            canbeInlined = false;
        } else {
            visited = new Hashtable<>();
            canbeInlined = !findLoop(this, this);
            if (body.stmts().size() > 3)
                canbeInlined = false;
            if (canbeInlined && Option.enableInlineFunction && Option.printInlineInfo)
                System.err.println(name() + " is inlined");
        }
    }
    private boolean findLoop(FunctionEntity called, FunctionEntity root) {
        if (visited.containsKey(called)) {
            return called == root;
        }

        visited.put(called, true);
        for (FunctionEntity func : called.calls()) {
            if (findLoop(func, root))
                return true;
        }
        return false;
    }

    // for locating local variabes
    public List<VariableEntity> allLocalVariables() {
        return scope.allLocalVariables();
    }

    // getter and setter
    public List<ParameterEntity> params() {
        return params;
    }

    public BlockNode body() {
        return body;
    }

    public Scope scope() {
        return scope;
    }

    public Type returnType() {
        return returnType;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public void setConstructor(boolean constructor) {
        isConstructor = constructor;
    }

    public List<IR> IR() {
        return irs;
    }

    public void setIR(List<IR> irs) {
        this.irs = irs;
    }

    public List<Instruction> INS() {
        return ins;
    }

    public void setINS(List<Instruction> ins) {
        this.ins = ins;
    }

    public List<Instruction> ins() {
        return ins;
    }

    public List<Reference> tmpStack() {
        return tmpStack;
    }

    public void setTmpStack(List<Reference> tmpStack) {
        this.tmpStack = tmpStack;
    }

    public String asmName() {
        return asmName == null ? name : asmName;
    }

    public void setAsmName(String name) {
        this.asmName = name;
    }

    public List<FunctionEntity> calls() {
        return calls;
    }

    public void addCall(FunctionEntity entity) {
        calls.add(entity);
    }

    public boolean canbeInlined() {
        return canbeInlined;
    }
    
    public void setLabelIR(com.mercy.compiler.IR.Label begin, com.mercy.compiler.IR.Label end) {
        this.beginLabelIR = begin;
        this.endLabelIR   = end;
    }

    public com.mercy.compiler.IR.Label beginLabelIR() {
        return beginLabelIR;
    }

    public com.mercy.compiler.IR.Label endLabelIR() {
        return endLabelIR;
    }

    public void setLabelINS(com.mercy.compiler.INS.Label begin, com.mercy.compiler.INS.Label end) {
        this.beginLabelINS = begin;
        this.endLabelINS   = end;
    }
    
    public Label beginLabelINS() {
        return beginLabelINS;
    }

    public Label endLabelINS() {
        return endLabelINS;
    }

    public List<BasicBlock> bbs() {
        return bbs;
    }

    public void setBbs(List<BasicBlock> bbs) {
        this.bbs = bbs;
    }

    public int frameSize() {
        return frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }

    public List<Register> regUsed() {
        return regUsed;
    }

    public void setRegUsed(List<Register> regUsed) {
        this.regUsed = regUsed;
    }

    public Set<Reference> allReference() {
        return allReference;
    }

    public void setAllReference(Set<Reference> allReference) {
        this.allReference = allReference;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "function entity : " + name;
    }
}
