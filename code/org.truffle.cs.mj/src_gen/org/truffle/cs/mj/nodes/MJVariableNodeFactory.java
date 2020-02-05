// CheckStyle: start generated
package org.truffle.cs.mj.nodes;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import org.truffle.cs.mj.nodes.MJExpressionNode;
import org.truffle.cs.mj.nodes.MJVariableNode;
import org.truffle.cs.mj.nodes.MJVariableNode.MJReadLocalVariableNode;
import org.truffle.cs.mj.nodes.MJVariableNode.MJWriteLocalVariableNode;

@GeneratedBy(MJVariableNode.class)
public final class MJVariableNodeFactory {

    @GeneratedBy(MJReadLocalVariableNode.class)
    public static final class MJReadLocalVariableNodeGen extends MJReadLocalVariableNode {

        private final FrameSlot slot;

        private MJReadLocalVariableNodeGen(FrameSlot slot) {
            this.slot = slot;
        }

        @Override
        protected FrameSlot getSlot() {
            return this.slot;
        }

        @Override
        public Object executeGeneric(VirtualFrame frameValue) {
            return readVariable(frameValue);
        }

        @Override
        public boolean executeBool(VirtualFrame frameValue) {
            return (boolean) executeGeneric(frameValue);
        }

        @Override
        public int executeI32(VirtualFrame frameValue) {
            return (int) executeGeneric(frameValue);
        }

        @Override
        public NodeCost getCost() {
            return NodeCost.MONOMORPHIC;
        }

        public static MJReadLocalVariableNode create(FrameSlot slot) {
            return new MJReadLocalVariableNodeGen(slot);
        }

    }
    @GeneratedBy(MJWriteLocalVariableNode.class)
    public static final class MJWriteLocalVariableNodeGen extends MJWriteLocalVariableNode {

        private final FrameSlot slot;
        @Child private MJExpressionNode value_;
        @CompilationFinal private int state_;

        private MJWriteLocalVariableNodeGen(MJExpressionNode value, FrameSlot slot) {
            this.slot = slot;
            this.value_ = value;
        }

        @Override
        protected FrameSlot getSlot() {
            return this.slot;
        }

        @Override
        public Object execute(VirtualFrame frameValue) {
            int state = state_;
            if ((state & 0b1110) == 0 /* only-active execute(VirtualFrame, int) */ && state != 0  /* is-not execute(VirtualFrame, int) && execute(VirtualFrame, char) && execute(VirtualFrame, long) && execute(VirtualFrame, Object) */) {
                return execute_int0(frameValue, state);
            } else {
                return execute_generic1(frameValue, state);
            }
        }

        private Object execute_int0(VirtualFrame frameValue, int state) {
            int valueValue_ = this.value_.executeI32(frameValue);
            assert (state & 0b1) != 0 /* is-active execute(VirtualFrame, int) */;
            return execute(frameValue, valueValue_);
        }

        private Object execute_generic1(VirtualFrame frameValue, int state) {
            Object valueValue_ = this.value_.executeGeneric(frameValue);
            if ((state & 0b1) != 0 /* is-active execute(VirtualFrame, int) */ && valueValue_ instanceof Integer) {
                int valueValue__ = (int) valueValue_;
                return execute(frameValue, valueValue__);
            }
            if ((state & 0b10) != 0 /* is-active execute(VirtualFrame, char) */ && valueValue_ instanceof Character) {
                char valueValue__ = (char) valueValue_;
                return execute(frameValue, valueValue__);
            }
            if ((state & 0b100) != 0 /* is-active execute(VirtualFrame, long) */ && valueValue_ instanceof Long) {
                long valueValue__ = (long) valueValue_;
                return execute(frameValue, valueValue__);
            }
            if ((state & 0b1000) != 0 /* is-active execute(VirtualFrame, Object) */) {
                return execute(frameValue, valueValue_);
            }
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(frameValue, valueValue_);
        }

        private Object executeAndSpecialize(VirtualFrame frameValue, Object valueValue) {
            int state = state_;
            if (valueValue instanceof Integer) {
                int valueValue_ = (int) valueValue;
                this.state_ = state = state | 0b1 /* add-active execute(VirtualFrame, int) */;
                return execute(frameValue, valueValue_);
            }
            if (valueValue instanceof Character) {
                char valueValue_ = (char) valueValue;
                this.state_ = state = state | 0b10 /* add-active execute(VirtualFrame, char) */;
                return execute(frameValue, valueValue_);
            }
            if (valueValue instanceof Long) {
                long valueValue_ = (long) valueValue;
                this.state_ = state = state | 0b100 /* add-active execute(VirtualFrame, long) */;
                return execute(frameValue, valueValue_);
            }
            this.state_ = state = state | 0b1000 /* add-active execute(VirtualFrame, Object) */;
            return execute(frameValue, valueValue);
        }

        @Override
        public NodeCost getCost() {
            int state = state_;
            if (state == 0b0) {
                return NodeCost.UNINITIALIZED;
            } else if ((state & (state - 1)) == 0 /* is-single-active  */) {
                return NodeCost.MONOMORPHIC;
            }
            return NodeCost.POLYMORPHIC;
        }

        public static MJWriteLocalVariableNode create(MJExpressionNode value, FrameSlot slot) {
            return new MJWriteLocalVariableNodeGen(value, slot);
        }

    }
}
