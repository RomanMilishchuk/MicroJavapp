package org.truffle.cs.mj.parser.identifiertable.types.primitives.constants;

import org.truffle.cs.mj.parser.identifiertable.types.primitives.IntDescriptor;

public class ConstantIntDescriptor extends IntDescriptor implements ConstantTypeDescriptor {
    private static ConstantIntDescriptor instance = new ConstantIntDescriptor();

    @Override
    public ConstantIntDescriptor getInstance() {
        return instance;
    }

}
