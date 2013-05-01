package com.amd.aparapi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gfrost
 * Date: 4/27/13
 * Time: 9:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class RegISA {
   static abstract class Type {
      static final u1 u1 = new u1();
      static final u8 u8 = new u8();
      static final u16 u16 = new u16();
      static final u32 u32 = new u32();
      static final u64 u64 = new u64();
      static final s8 s8 = new s8();
      static final s16 s16 = new s16();
      static final s32 s32 = new s32();
      static final s64 s64 = new s64();

      static final f16 f16 = new f16();
      static final f32 f32 = new f32();
      static final f64 f64 = new f64();
      int bits;
      public int getBits(){
         return (bits);
      }
      String sizeType; // s,d
      public String getSizeType(){
         return(sizeType);
      }
      String type; // u,f,s

      public String getType(){
         return(type);
      }
      Type(int _bits, String _sizeType, String _type){
          bits = _bits;
         sizeType = _sizeType;
         type = _type;
      }

      String getRegName(int i){
         return("$"+sizeType+""+i);
      }
      String getTypeName(){
         return(type+bits);
      }


   };
   static class u1 extends Type{

      u1(){
         super(1, "?", "u");
      }
   }
   static class u8 extends Type{
      u8(){
         super(8, "?", "u");
      }

   }
   static class s8 extends Type{
      s8(){
         super(8, "?", "s");
      }
   }
   static class u16 extends Type{
      u16(){
         super(16, "?", "u");
      }
   }
   static class s16 extends Type{
      s16(){
         super(16, "?", "s");
      }
   }
   static class f16 extends Type{
      f16(){
         super(16, "?", "f");
      }
   }
   static class u32 extends Type{
      u32(){
         super(32, "s", "u");
      }
   }
   static class s32 extends Type{
      s32(){
         super(32, "s", "s");
      }
   }
   static class f32 extends Type{
      f32(){
         super(32, "s", "f");
      }
   }
   static class u64 extends Type{
      u64(){
         super(64, "d", "u");
      }
   }
   static class s64 extends Type{
      s64(){
         super(64, "d", "s");
      }
   }
   static class f64 extends Type{
      f64(){
         super(64, "d", "f");
      }
   }

    static class Reg<T>{
       int index;
       T type;
       Reg(int _index, T _type){
          index = _index;
          type = _type;
       }
      
    }


    static abstract class RegInstruction{
        Instruction from;
        RegInstruction(Instruction _from){
            from = _from;
        }

        abstract void render(RegISARenderer r);

    }

    static class branch extends RegInstruction{
        String name;
        int pc;
        branch(Instruction _from, String _name, int _pc){
            super(_from);
            name = _name;
            pc = _pc;
        }
        @Override public void render(RegISARenderer r){
            r.append(name+" ");
            r.label(pc);
        }
    }

    static class field extends RegInstruction{
        field(Instruction _from){
            super(_from);
        }

        @Override void render(RegISARenderer r){
            r.field();
            TypeHelper.Type type = from.asFieldAccessor().getConstantPoolFieldEntry().getType();
            String dotClassName = from.asFieldAccessor().getConstantPoolFieldEntry().getClassEntry().getDotClassName();
            String name = from.asFieldAccessor().getConstantPoolFieldEntry().getName();
            if(from instanceof InstructionSet.I_PUTFIELD || from instanceof InstructionSet.I_PUTSTATIC){
                if(type.isArray()){
                    r.append("arr_");
                }
                if(type.isInt()){
                    r.s32().space().append(dotClassName).dot().append(name).separator().s32Name(from.getPreStackBaseOnLocals());
                }
                if(type.isFloat()){
                    r.f32().space().append(dotClassName).dot().append(name).separator().f32Name(from.getPreStackBaseOnLocals());
                }
            }else{
                if(type.isArray()){
                    r.append("arr_");
                }
                if(type.isInt()){
                    r.append("s32 " + ((type.isArray())?"arr_":""));
                    r.s32Name(from.getPreStackBaseOnLocals());
                }
                if(type.isFloat()){
                    r.append("f32 " + ((type.isArray())?"arr_":""));
                    r.f32Name(from.getPreStackBaseOnLocals());
                }
                r.separator().u64Name(from.getPreStackBaseOnLocals()).separator().append(dotClassName).dot().append(name);
            }
        }

    }
    static class call extends RegInstruction{

        call(Instruction _from){
            super(_from);
        }

        @Override void render(RegISARenderer r){
            String dotClassName = from.asMethodCall().getConstantPoolMethodEntry().getClassEntry().getDotClassName();
            String name = from.asMethodCall().getConstantPoolMethodEntry().getName();
            TypeHelper.ArgsAndReturnType argsAndReturnType = from.asMethodCall().getConstantPoolMethodEntry().getArgsAndReturnType();


            TypeHelper.Type returnType = argsAndReturnType.getReturnType();


            if(returnType.isVoid()){
                r.call().append("void").space().append("VOID");
            }else if(returnType.isInt()){
                r.call().s32().space().s32Name(from.getPreStackBaseOnLocals());

            }else if(returnType.isDouble()){
                r.call().f64().space().f64Name(from.getPreStackBaseOnLocals());


            }
            r.separator().append(dotClassName).dot().append(name).space();

            for(TypeHelper.Arg arg : argsAndReturnType.getArgs()){
                if(arg.getArgc() > 0){
                    r.separator();
                }
                if(arg.isDouble()){
                    r.f64Name(from.getPreStackBaseOnLocals() + arg.getArgc());
                }else if(arg.isFloat()){
                    r.f32Name( from.getPreStackBaseOnLocals() + arg.getArgc());
                }else if(arg.isInt()){
                    r.s32Name( from.getPreStackBaseOnLocals() + arg.getArgc());
                }else if(arg.isLong()){
                    r.s64Name( from.getPreStackBaseOnLocals() + arg.getArgc());
                }
            }
        }
    }

    static class nyi extends RegInstruction{

        nyi(Instruction _from){
            super(_from);
        }

        @Override void render(RegISARenderer r){

            r.append("NYI "+from.getByteCode().getName());//InstructionHelper.getLabel(i, false, false, false);
            if(from.isBranch()){
                r.append(" " + from.asBranch().getAbsolute());
            }else if(from.isFieldAccessor()){
                r.append(" " + from.asFieldAccessor().getConstantPoolFieldEntry().getType());
                r.append(" " + from.asFieldAccessor().getConstantPoolFieldEntry().getClassEntry().getDotClassName());
                r.append(" " + from.asFieldAccessor().getConstantPoolFieldEntry().getName());
            }else if(from.isLocalVariableAccessor()){
                r.append(" #" + from.asLocalVariableAccessor().getLocalVariableInfo().getSlot());
                r.append(" " + from.asLocalVariableAccessor().getLocalVariableInfo().getVariableName());
                r.append(" " + from.asLocalVariableAccessor().getLocalVariableInfo().getVariableDescriptor());

            }else if(from.isMethodCall()){
                r.append(" " + from.asMethodCall().getConstantPoolMethodEntry().getArgsAndReturnType().getReturnType());
                r.append(" " + from.asMethodCall().getConstantPoolMethodEntry().getClassEntry().getDotClassName());
                r.append("." + from.asMethodCall().getConstantPoolMethodEntry().getName());
            }else if(from.isConstant()){
                r.append("." + from.asConstant().getValue());
            }

        }
    }

    static class ld_kernarg<T extends Type> extends RegInstruction{
        T type;
        int argc;

        ld_kernarg(Instruction _from, T _type, int _argc){
            super(_from);
           type = _type;
           argc = _argc;
        }

       @Override void render(RegISARenderer r){
          r.arg().append(type.getTypeName()).space().append(type.getRegName(argc)).separator().argRef(argc);
       }
    }


 

    static class add_const_s32 extends RegInstruction{
        int reg_dest, reg_src, value;
        add_const_s32(Instruction _from, int _reg_dest, int _reg_src, int _value){
            super(_from);
            reg_dest=_reg_dest;
            reg_src = _reg_src;
            value = _value;
        }
        @Override void render(RegISARenderer r){
            r.add().s32().space().s32Name(reg_dest).separator().s32Name(reg_src).separator().append(value);
        }
    }


    static class cvt<T1 extends Type, T2 extends Type> extends RegInstruction{
        Reg<T1> dest;
        Reg<T2> src;
        cvt(Instruction _from,  Reg<T1> _dest, Reg<T2> _src){
            super(_from);
            dest=_dest;
            src = _src;
        }
        @Override void render(RegISARenderer r){
            r.cvt().append(dest.type.getTypeName()).append("_").append(src.type.getTypeName()).space()
                    .append(dest.type.getRegName(dest.index)).separator().append(src.type.getRegName(src.index));
        }
    }



   static class ret extends RegInstruction{

      ret(Instruction _from){
         super(_from);

      }

      @Override void render(RegISARenderer r){
         r.append("ret");
      }
   }

    static abstract class store extends RegInstruction{
        Type type;
        Reg reg_src, reg_mem;
        store(Instruction _from, Reg _reg_mem, Reg _reg_src){
            super(_from);

            reg_mem = _reg_mem;
            reg_src = _reg_src;
        }
    }

    static class store_s32 extends store{

        store_s32(Instruction _from,  Reg _reg_mem, Reg _reg_src){
            super(_from, _reg_mem, _reg_src);
        }

        @Override void render(RegISARenderer r){
            r.store().s32().space().s32Array(reg_mem.index).separator().s32Name(reg_src.index);
        }
    }


    static abstract class load extends RegInstruction{
        int reg_mem;
         int reg_dest;
        load(Instruction _from, int _reg_dest, int _reg_mem){
            super(_from);
            reg_dest = _reg_dest;
            reg_mem = _reg_mem;
        }
    }
    static class load_s32 extends load{

        load_s32(Instruction _from, int _reg_dest, int _reg_mem){
            super(_from, _reg_dest, _reg_mem);
        }

        @Override void render(RegISARenderer r){
            r.load().s32().space().s32Name(reg_dest).separator().s32Array(reg_mem);
        }
    }
    static class load_f32 extends load{

        load_f32(Instruction _from, int _reg_dest, int _reg_mem){
            super(_from, _reg_dest, _reg_mem);
        }
        @Override void render(RegISARenderer r){
            r.load().f32().space().f32Name(reg_dest).separator().f32Array(reg_mem);
        }
    }
    static class load_s64 extends load{

        load_s64(Instruction _from, int _reg_dest, int _reg_mem){
            super(_from, _reg_dest, _reg_mem);
        }
        @Override void render(RegISARenderer r){
            r.load().s64().space().s64Name(reg_dest).separator().s64Array(reg_mem);
        }
    }



    static abstract class mov extends RegInstruction{
        int reg_dest, reg_src;

        public mov(Instruction _from, int _reg_dest, int _reg_src){
            super(_from);
            reg_dest = _reg_dest;
            reg_src = _reg_src;
        }
    }

    static abstract class binary extends RegInstruction{
        int reg_dest, reg_lhs, reg_rhs;

        public binary(Instruction _from, int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from);
            reg_dest = _reg_dest;
            reg_lhs = _reg_lhs;
            reg_rhs = _reg_rhs;
        }
    }

   static abstract class binaryRegConst<T> extends RegInstruction{
      int reg_dest, reg_lhs;
      T value_rhs;

      public binaryRegConst(Instruction _from, int _reg_dest, int _reg_lhs, T _value_rhs){
         super(_from);
         reg_dest = _reg_dest;
         reg_lhs = _reg_lhs;
         value_rhs = _value_rhs;
      }
   }

   static abstract class addConst<T> extends binaryRegConst<T>{

      public addConst(Instruction _from,int _reg_dest, int _reg_lhs, T _value_rhs){
         super(_from, _reg_dest, _reg_lhs, _value_rhs);
      }
   }

    static abstract class add extends binary{
        public add(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from, _reg_dest, _reg_lhs, _reg_rhs);
        }
    }

    static class add_s32 extends add{

        public add_s32(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }

        @Override void render(RegISARenderer r){
            r.add().s32().space().s32Name(reg_dest).separator().s32Name(reg_lhs).separator().s32Name(reg_rhs);
        }
    }

    static  class add_s64 extends add{

        public add_s64(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
        @Override void render(RegISARenderer r){
            r.add().s64().space().s64Name(reg_dest).separator().s64Name(reg_lhs).separator().s64Name(reg_rhs);
        }
    }

   static  class add_u64 extends add{
      public add_u64(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
         super(_from,_reg_dest, _reg_lhs, _reg_rhs);
      }
      @Override void render(RegISARenderer r){
         r.add().u64().space().u64Name(reg_dest).separator().u64Name(reg_lhs).separator().u64Name(reg_rhs);
      }
   }
    static  class add_f32 extends add{

        public add_f32(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
        @Override void render(RegISARenderer r){
            r.add().f32().space().f32Name(reg_dest).separator().f32Name(reg_lhs).separator().f32Name(reg_rhs);
        }
    }
    static  class add_f64 extends add{

        public add_f64(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
        @Override void render(RegISARenderer r){
            r.add().f64().space().f64Name(reg_dest).separator().f64Name(reg_lhs).separator().f64Name(reg_rhs);
        }
    }
    static abstract class sub extends binary{

        public sub(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }

    }
    static class sub_s32 extends sub{

        public sub_s32(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
        @Override void render(RegISARenderer r){
            r.sub().s32().space().s32Name(reg_dest).separator().s32Name(reg_lhs).separator().s32Name(reg_rhs);
        }
    }

    static class sub_f64 extends sub{

        public sub_f64(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
        @Override void render(RegISARenderer r){
            r.sub().f64().space().f64Name(reg_dest).separator().f64Name(reg_lhs).separator().f64Name(reg_rhs);
        }
    }

    static class sub_f32 extends sub{

        public sub_f32(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
        @Override void render(RegISARenderer r){
            r.sub().f32().space().f32Name(reg_dest).separator().f32Name(reg_lhs).separator().f32Name(reg_rhs);
        }
    }
    static abstract class div extends binary{

        public div(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
    }
    static class div_s32 extends div{

        public div_s32(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
        @Override void render(RegISARenderer r){
            r.div().s32().space().s32Name(reg_dest).separator().s32Name(reg_lhs).separator().s32Name(reg_rhs);
        }
    }
    static class div_f32 extends div{

        public div_f32(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
        @Override void render(RegISARenderer r){
            r.div().f32().space().f32Name(reg_dest).separator().f32Name(reg_lhs).separator().f32Name(reg_rhs);
        }
    }
    static abstract class mul extends binary{

        public mul(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
    }
    static class mul_s32 extends mul{

        public mul_s32(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
        @Override void render(RegISARenderer r){
            r.mul().s32().space().s32Name(reg_dest).separator().s32Name(reg_lhs).separator().s32Name(reg_rhs);
        }
    }
   static class mul_u64 extends mul{

      public mul_u64(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
         super(_from,_reg_dest, _reg_lhs, _reg_rhs);
      }
      @Override void render(RegISARenderer r){
         r.mul().u64().space().u64Name(reg_dest).separator().u64Name(reg_lhs).separator().u64Name(reg_rhs);
      }
   }
   static class mul_f32 extends mul{

        public mul_f32(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
        @Override void render(RegISARenderer r){
            r.mul().f32().space().f32Name(reg_dest).separator().f32Name(reg_lhs).separator().f32Name(reg_rhs);
        }
    }
    static abstract class rem extends binary{

        public rem(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
    }
    static class rem_s32 extends rem{

        public rem_s32(Instruction _from,int _reg_dest, int _reg_lhs, int _reg_rhs){
            super(_from,_reg_dest, _reg_lhs, _reg_rhs);
        }
        @Override void render(RegISARenderer r){
            r.rem().s32().space().s32Name(reg_dest).separator().s32Name(reg_lhs).separator().s32Name(reg_rhs);
        }
    }


    static  class mov_const<R extends Type,T> extends RegInstruction{
        Reg<R> dest;
        T value;

        public mov_const(Instruction _from,Reg<R> _dest, T _value){
            super(_from);
            dest = _dest;
            value = _value;
        }

        @Override void render(RegISARenderer r){
            r.mov().append(dest.type.getTypeName()).space().append(dest.type.getRegName(dest.index)).separator().append(value.toString());

        }
    }



    static class mov_s32 extends mov{

        public mov_s32(Instruction _from,int _reg_dest, int _reg_src){
            super(_from,_reg_dest, _reg_src);
        }

        @Override void render(RegISARenderer r){
            r.mov().s32().space().s32Name(reg_dest).separator().s32Name(reg_src);

        }
    }

    static class mov_s64 extends mov{

        public mov_s64(Instruction _from,int _reg_dest, int _reg_src){
            super(_from,_reg_dest, _reg_src);
        }
        @Override void render(RegISARenderer r){
            r.mov().s64().space().s64Name(reg_dest).separator().s64Name(reg_src);

        }
    }
    static class mov_f64 extends mov{
        public mov_f64(Instruction _from,int _reg_dest, int _reg_src){
            super(_from,_reg_dest, _reg_src);
        }
        @Override void render(RegISARenderer r){
            r.mov().f64().space().f64Name(reg_dest).separator().f64Name(reg_src);

        }
    }
    static class mov_f32 extends mov{
        public mov_f32(Instruction _from,int _reg_dest, int _reg_src){
            super(_from,_reg_dest, _reg_src);
        }
        @Override void render(RegISARenderer r){
            r.mov().f32().space().f32Name(reg_dest).separator().f32Name(reg_src);

        }
    }
    static class mov_u64 extends mov{
        public mov_u64(Instruction _from,int _reg_dest, int _reg_src){
            super(_from,_reg_dest, _reg_src);
        }
        @Override void render(RegISARenderer r){
            r.mov().u64().space().u64Name(reg_dest).separator().u64Name(reg_src);

        }
    }

    List<RegInstruction> instructions = new ArrayList<RegInstruction>();
    ClassModel.ClassModelMethod method;

    void add(RegInstruction _regInstruction){
        instructions.add(_regInstruction);
    }




    String argType(TypeHelper.Arg _arg){
        if (_arg.isArray()){
            return("u64");
        }else if (_arg.isInt()){
            return("s32");
        }else if (_arg.isFloat()){
            return("f32");
        }else{
            return("?");
        }
    }
    String regType(TypeHelper.Arg _arg){
        if (_arg.isArray()){
            return("d");
        }else if (_arg.isInt()){
            return("s");
        }else if (_arg.isFloat()){
            return("s");
        }else{
            return("?");
        }
    }




    RegISARenderer render(RegISARenderer r){
        r.append("version 1:0").nl();
        r.append("kernel &"+method.getName()+"(");
        int argOffset = method.isStatic()?0:1;
        if (!method.isStatic()){
            r.nl().indent().append("kernarg_u64 %_arg0");
        }

        for (TypeHelper.Arg arg:method.argsAndReturnType.getArgs()){
            if ((method.isStatic() && arg.getArgc()==0) ){
                r.nl();
            }           else{
                r.separator().nl();
            }
            r.indent().append("kernarg_"+argType(arg)+" %_arg"+(arg.getArgc()+argOffset));

        }
        r.nl().indent().append("){").nl();

        java.util.Set<Instruction> s = new java.util.HashSet<Instruction>();

        for(RegInstruction i : instructions){
            if (i.from.isBranchTarget() && !s.contains(i.from)){
                s.add(i.from);
                r.label(i.from.getThisPC());
                r.nl();
            }
            r.indent();
            i.render(r);
            r.semicolon();
            r.nl();
        }
        r.append("};");
        return(r);
    }



    private void add(Instruction instruction){

        switch(instruction.getByteCode()){

            case ACONST_NULL:
                add(new nyi(instruction));
                break;
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
            case BIPUSH:
            case SIPUSH:  {
                InstructionSet.Constant<Integer> c = ( InstructionSet.Constant) instruction;
                add(new mov_const<s32, Integer>(instruction, new Reg<s32>(instruction.getPreStackBaseOnLocals(), Type.s32), c.getValue()));
            }
            break;
            case LCONST_0:
            case LCONST_1:
            {
                InstructionSet.Constant<Long> c = ( InstructionSet.Constant) instruction;
                add(new mov_const<s64, Long>(instruction, new Reg<s64>(instruction.getPreStackBaseOnLocals(), Type.s64), c.getValue()));
            }
            break;
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
            {
                InstructionSet.Constant<Float> c = ( InstructionSet.Constant) instruction;
                add(new mov_const<f32, Float>(instruction,new Reg<f32>(instruction.getPreStackBaseOnLocals(), Type.f32), c.getValue()));
            }

            break;
            case DCONST_0:
            case DCONST_1:
            {
                InstructionSet.Constant<Double> c = ( InstructionSet.Constant) instruction;
                add(new mov_const<f64, Double>(instruction, new Reg<f64>(instruction.getPreStackBaseOnLocals(), Type.f64), c.getValue()));

            }
            break;
            // case BIPUSH: moved up
            // case SIPUSH: moved up

            case LDC:
            case LDC_W:
            case LDC2_W: {
                InstructionSet.ConstantPoolEntryConstant cpe = (InstructionSet.ConstantPoolEntryConstant )instruction;

                ClassModel.ConstantPool.ConstantEntry e= (ClassModel.ConstantPool.ConstantEntry )cpe.getConstantPoolEntry() ;
                if (e instanceof ClassModel.ConstantPool.DoubleEntry){
                    add(new mov_const<f64, Double>(instruction, new Reg<f64>(instruction.getPreStackBaseOnLocals(), Type.f64), ((ClassModel.ConstantPool.DoubleEntry)e).getValue()));

                } else if (e instanceof ClassModel.ConstantPool.FloatEntry){
                    add(new mov_const<f32, Float>(instruction, new Reg<f32>(instruction.getPreStackBaseOnLocals(), Type.f32), ((ClassModel.ConstantPool.FloatEntry)e).getValue()));


                }  else if (e instanceof ClassModel.ConstantPool.IntegerEntry){
                    add(new mov_const<s32, Integer>(instruction, new Reg<s32>(instruction.getPreStackBaseOnLocals(), Type.s32), ((ClassModel.ConstantPool.IntegerEntry)e).getValue()));

                }  else if (e instanceof ClassModel.ConstantPool.LongEntry){
                    add(new mov_const<s64, Long>(instruction, new Reg<s64>(instruction.getPreStackBaseOnLocals(), Type.s64), ((ClassModel.ConstantPool.LongEntry)e).getValue()));

                }

            }
            break;
            // case LLOAD: moved down
            // case FLOAD: moved down
            // case DLOAD: moved down
            //case ALOAD: moved down
            case ILOAD:
            case ILOAD_0:
            case ILOAD_1:
            case ILOAD_2:
            case ILOAD_3:
                add(new mov_s32(instruction,instruction.getPreStackBaseOnLocals(),
                        instruction.asLocalVariableAccessor().getLocalVariableTableIndex()));

                break;
            case LLOAD:
            case LLOAD_0:
            case LLOAD_1:
            case LLOAD_2:
            case LLOAD_3:
                add(new mov_s64(instruction,instruction.getPreStackBaseOnLocals(),
                        instruction.asLocalVariableAccessor().getLocalVariableTableIndex()));
                break;
            case FLOAD:
            case FLOAD_0:
            case FLOAD_1:
            case FLOAD_2:
            case FLOAD_3:
                add(new mov_f32(instruction,instruction.getPreStackBaseOnLocals(),
                        instruction.asLocalVariableAccessor().getLocalVariableTableIndex()));
                break;
            case DLOAD:
            case DLOAD_0:
            case DLOAD_1:
            case DLOAD_2:
            case DLOAD_3:
                add(new mov_f64(instruction,instruction.getPreStackBaseOnLocals(),
                        instruction.asLocalVariableAccessor().getLocalVariableTableIndex()));
                break;
            case ALOAD:
            case ALOAD_0:
            case ALOAD_1:
            case ALOAD_2:
            case ALOAD_3:
                add(new mov_u64(instruction,instruction.getPreStackBaseOnLocals(),
                        instruction.asLocalVariableAccessor().getLocalVariableTableIndex()));
                break;
            case IALOAD:
            {
               add(new cvt<u64,s32>(instruction,new Reg<u64>(instruction.getPreStackBaseOnLocals() + 1, Type.u64),
                       new Reg<s32>(instruction.getPreStackBaseOnLocals() + 1, Type.s32 ))) ;  // index converted to 64 bit

                add(new mov_const<s64, Long>(instruction, new Reg<s64>(instruction.getPostStackBaseOnLocals(), Type.s64), (long)4));

                                                      // load 4 into scratch
               add(new mul_u64(instruction,instruction.getPreStackBaseOnLocals() + 1, instruction.getPreStackBaseOnLocals() + 1, instruction.getPostStackBaseOnLocals() ));
               add(new add_u64(instruction, instruction.getPreStackBaseOnLocals()+1,  instruction.getPreStackBaseOnLocals() + 0, instruction.getPreStackBaseOnLocals()+1 )) ;
               add(new load_s32( instruction,
                     instruction.getPreStackBaseOnLocals(),   //dest
                     instruction.getPreStackBaseOnLocals()+ 1)); //mem
            }


                break;
            case LALOAD:
                add(new load_s64( instruction,
                        instruction.getPreStackBaseOnLocals(),   //array & value
                        instruction.getPreStackBaseOnLocals()+ 1)); //index
                break;
            case FALOAD:
                add(new load_f32( instruction,
                        instruction.getPreStackBaseOnLocals(),   //array & value
                        instruction.getPreStackBaseOnLocals() + 1)); //index
                break;
            case DALOAD:
                add(new nyi(instruction));
                break;
            case AALOAD:
                add(new nyi(instruction));
                break;
            case BALOAD:
                add(new nyi(instruction));
                break;
            case CALOAD:
                add(new nyi(instruction));
                break;
            case SALOAD:
                add(new nyi(instruction));
                break;
            //case ISTORE: moved down
            // case LSTORE:  moved down
            //case FSTORE: moved down
            //case DSTORE:  moved down
            // case ASTORE: moved down
            case ISTORE:
            case ISTORE_0:
            case ISTORE_1:
            case ISTORE_2:
            case ISTORE_3:
                add(new mov_s32(instruction, instruction.asLocalVariableAccessor().getLocalVariableTableIndex(),
                        instruction.getPreStackBaseOnLocals()));

                break;
            case LSTORE:
            case LSTORE_0:
            case LSTORE_1:
            case LSTORE_2:
            case LSTORE_3:
                add(new mov_s64(instruction, instruction.asLocalVariableAccessor().getLocalVariableTableIndex(),
                        instruction.getPreStackBaseOnLocals()));

                break;
            case FSTORE:
            case FSTORE_0:
            case FSTORE_1:
            case FSTORE_2:
            case FSTORE_3:
                add(new mov_f32(instruction, instruction.asLocalVariableAccessor().getLocalVariableTableIndex(),
                        instruction.getPreStackBaseOnLocals()));
                break;
            case DSTORE:
            case DSTORE_0:
            case DSTORE_1:
            case DSTORE_2:
            case DSTORE_3:
                add(new mov_f64(instruction, instruction.asLocalVariableAccessor().getLocalVariableTableIndex(),
                        instruction.getPreStackBaseOnLocals()));
                break;
            case ASTORE:
            case ASTORE_0:
            case ASTORE_1:
            case ASTORE_2:
            case ASTORE_3:
                add(new mov_u64(instruction, instruction.asLocalVariableAccessor().getLocalVariableTableIndex(),
                        instruction.getPreStackBaseOnLocals()));
                break;
            case IASTORE:

            {
               // int scratchBase = instruction.getPostStackBase()+method.getCodeEntry().getMaxLocals(); // we are safe using any register above this
                /*

                cvt_u64_s32 $d(index), $s(index)
                mul_u64 $d(index), $d(index), (sizeof array element);
                add_u64 $d(index), $d(index), $d{array};
                st_global_s32 $s3, [$d6 + 24];
                 */
                add(new cvt<u64,s32>(instruction,new Reg<u64>(instruction.getPreStackBaseOnLocals() + 1, Type.u64),
                        new Reg<s32>(instruction.getPreStackBaseOnLocals() + 1, Type.s32 ))) ;  // index converted to 64 bit

                 add(new mov_const<s64, Long>(instruction, new Reg<s64>(instruction.getPostStackBaseOnLocals(), Type.s64), (long)4));                                           // load 4 into scratch
                add(new mul_u64(instruction,instruction.getPreStackBaseOnLocals() + 1, instruction.getPreStackBaseOnLocals() + 1, instruction.getPostStackBaseOnLocals() ));
                add(new add_u64(instruction, instruction.getPreStackBaseOnLocals()+1,  instruction.getPreStackBaseOnLocals() + 0, instruction.getPreStackBaseOnLocals()+1 )) ;
                add(new store_s32(instruction,
                        new Reg(instruction.getPreStackBaseOnLocals() + 1, Type.u64), //mem
                        new Reg(instruction.getPreStackBaseOnLocals()+ 2 , Type.s32)//reg
                      ));
            }
                break;
            case LASTORE:
                add(new nyi(instruction));
                break;
            case FASTORE:
                add(new nyi(instruction));
                break;
            case DASTORE:
                add(new nyi(instruction));
                break;
            case AASTORE:
                add(new nyi(instruction));
                break;
            case BASTORE:
                add(new nyi(instruction));
                break;
            case CASTORE:
                add(new nyi(instruction));
                break;
            case SASTORE:
                add(new nyi(instruction));
                break;
            case POP:
                add(new nyi(instruction));
                break;
            case POP2:
                add(new nyi(instruction));
                break;
            case DUP:
                add(new nyi(instruction));
                break;
            case DUP_X1:
                add(new nyi(instruction));
                break;
            case DUP_X2:
                add(new mov_s32(instruction, instruction.getPreStackBaseOnLocals()+3, instruction.getPreStackBaseOnLocals()+2) ) ;
                add(new mov_s32(instruction, instruction.getPreStackBaseOnLocals()+2, instruction.getPreStackBaseOnLocals()+1) ) ;

                add(new mov_s32(instruction, instruction.getPreStackBaseOnLocals()+1, instruction.getPreStackBaseOnLocals()+0)) ;

                add(new mov_s32(instruction, instruction.getPreStackBaseOnLocals()+0, instruction.getPreStackBaseOnLocals()+3 )) ;
                break;
            case DUP2:

                add(new mov_s32(instruction, instruction.getPreStackBaseOnLocals()+2, instruction.getPreStackBaseOnLocals()+0) ) ;
                add(new mov_s32(instruction, instruction.getPreStackBaseOnLocals()+3, instruction.getPreStackBaseOnLocals()+1) ) ;
                break;
            case DUP2_X1:
                add(new nyi(instruction));
                break;
            case DUP2_X2:
                add(new nyi(instruction));
                break;
            case SWAP:
                add(new nyi(instruction));
                break;
            case IADD:
                add(new add_s32(instruction,instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals()+1));

                break;
            case LADD:
                add(new add_s64(instruction,instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals()+1));
                break;
            case FADD:
                add(new add_f32(instruction,instruction.getPreStackBaseOnLocals(),instruction.getPreStackBaseOnLocals(),instruction.getPreStackBaseOnLocals()+1));
                break;
            case DADD:
                add(new nyi(instruction));
                break;
            case ISUB:
                add(new sub_s32(instruction,instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals()+1));

                break;
            case LSUB:
                add(new nyi(instruction));
                break;
            case FSUB:
                add(new sub_f32(instruction,instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals()+1));
                break;
            case DSUB:
                add(new nyi(instruction));
                break;
            case IMUL:
                add(new mul_s32(instruction,instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals()+1));

                break;
            case LMUL:
                add(new nyi(instruction));
                break;
            case FMUL:
                add(new mul_f32(instruction,instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals()+1));

                break;
            case DMUL:
                add(new nyi(instruction));
                break;
            case IDIV:
                add(new div_s32(instruction,instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals()+1));

                break;
            case LDIV:
                add(new nyi(instruction));
                break;
            case FDIV:
                add(new div_f32(instruction,instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals()+1));

                break;
            case DDIV:
                add(new nyi(instruction));
                break;
            case IREM:
                add(new rem_s32(instruction,instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals(), instruction.getPreStackBaseOnLocals()+1));

                break;
            case LREM:
                add(new nyi(instruction));
                break;
            case FREM:
                add(new nyi(instruction));
                break;
            case DREM:
                add(new nyi(instruction));
                break;
            case INEG:
                add(new nyi(instruction));
                break;
            case LNEG:
                add(new nyi(instruction));
                break;
            case FNEG:
                add(new nyi(instruction));
                break;
            case DNEG:
                add(new nyi(instruction));
                break;
            case ISHL:
                add(new nyi(instruction));
                break;
            case LSHL:
                add(new nyi(instruction));
                break;
            case ISHR:
                add(new nyi(instruction));
                break;
            case LSHR:
                add(new nyi(instruction));
                break;
            case IUSHR:
                add(new nyi(instruction));
                break;
            case LUSHR:
                add(new nyi(instruction));
                break;
            case IAND:
                add(new nyi(instruction));
                break;
            case LAND:
                add(new nyi(instruction));
                break;
            case IOR:
                add(new nyi(instruction));
                break;
            case LOR:
                add(new nyi(instruction));
                break;
            case IXOR:
                add(new nyi(instruction));
                break;
            case LXOR:
                add(new nyi(instruction));
                break;
            case IINC:
                add(new add_const_s32(instruction, ((InstructionSet.I_IINC)instruction).getLocalVariableTableIndex(),((InstructionSet.I_IINC)instruction).getLocalVariableTableIndex(), ((InstructionSet.I_IINC)instruction).getDelta()));

                break;
            case I2L:
                add(new nyi(instruction));
                break;
            case I2F:
                add(new cvt<s32,f32>(instruction,new Reg<s32>(instruction.getPreStackBaseOnLocals(), Type.s32),
                        new Reg<f32>(instruction.getPreStackBaseOnLocals(), Type.f32 ))) ;  // index converted to 64 bit



                break;
            case I2D:
                add(new nyi(instruction));
                break;
            case L2I:
                add(new nyi(instruction));
                break;
            case L2F:
                add(new nyi(instruction));
                break;
            case L2D:
                add(new nyi(instruction));
                break;
            case F2I:
                add(new nyi(instruction));
                break;
            case F2L:
                add(new nyi(instruction));
                break;
            case F2D:
                add(new nyi(instruction));
                break;
            case D2I:
                add(new nyi(instruction));
                break;
            case D2L:
                add(new nyi(instruction));
                break;
            case D2F:
                add(new nyi(instruction));
                break;
            case I2B:
                add(new nyi(instruction));
                break;
            case I2C:
                add(new nyi(instruction));
                break;
            case I2S:
                add(new nyi(instruction));
                break;
            case LCMP:
                add(new nyi(instruction));
                break;
            case FCMPL:
                add(new nyi(instruction));
                break;
            case FCMPG:
                add(new nyi(instruction));
                break;
            case DCMPL:
                add(new nyi(instruction));
                break;
            case DCMPG:
                add(new nyi(instruction));
                break;
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case GOTO:
            case IFNULL:
            case IFNONNULL:
            case GOTO_W:
                add(new branch(instruction, instruction.getByteCode().getName(), instruction.asBranch().getAbsolute()));
                break;
            case JSR:
                add(new nyi(instruction));
                break;
            case RET:
                add(new nyi(instruction));
                break;
            case TABLESWITCH:
                add(new nyi(instruction));
                break;
            case LOOKUPSWITCH:
                add(new nyi(instruction));
                break;
            case IRETURN:
                add(new nyi(instruction));
                break;
            case LRETURN:
                add(new nyi(instruction));
                break;
            case FRETURN:
                add(new nyi(instruction));
                break;
            case DRETURN:
                add(new nyi(instruction));
                break;
            case ARETURN:
                add(new nyi(instruction));
                break;
            case RETURN:
                add(new ret(instruction));
                break;
            case GETSTATIC:
            case PUTSTATIC:
            case GETFIELD:
            case PUTFIELD:
                add(new field(instruction));
                break;
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEINTERFACE:
            case INVOKEDYNAMIC:
                add(new call(instruction));
                break;
            case NEW:
                add(new nyi(instruction));
                break;
            case NEWARRAY:
                add(new nyi(instruction));
                break;
            case ANEWARRAY:
                add(new nyi(instruction));
                break;
            case ARRAYLENGTH:
                add(new nyi(instruction));
                break;
            case ATHROW:
                add(new nyi(instruction));
                break;
            case CHECKCAST:
                add(new nyi(instruction));
                break;
            case INSTANCEOF:
                add(new nyi(instruction));
                break;
            case MONITORENTER:
                add(new nyi(instruction));
                break;
            case MONITOREXIT:
                add(new nyi(instruction));
                break;
            case WIDE:
                add(new nyi(instruction));
                break;
            case MULTIANEWARRAY:
                add(new nyi(instruction));
                break;
            case JSR_W:
                add(new nyi(instruction));
                break;

        }

    }

    RegISA(ClassModel.ClassModelMethod _method){


       method = _method;




       for(Instruction i : method.getInstructions()){
            if (i.getThisPC()==0){
               int argOffset = 0;
               if (!method.isStatic()){
                  add(new ld_kernarg(i, Type.u64, 0));
                  argOffset++;
               }
               for (TypeHelper.Arg arg:method.argsAndReturnType.getArgs()){
                  if (arg.isArray()){
                     add(new ld_kernarg(i, Type.u64, arg.getArgc()+argOffset));
                  }else if (arg.isInt()){
                     add(new ld_kernarg(i, Type.s32, arg.getArgc()+argOffset));


                  }
               }
            }
            add( i);
        }
    }
}
