/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package packed.internal.util;

import java.lang.reflect.AnnotatedElement;

import app.packed.util.FieldDescriptor;
import app.packed.util.VariableDescriptor;

/**
 *
 */
public final class ErrorMessageBuilder implements CharSequence {

    StringBuilder sb = new StringBuilder();

    public ErrorMessageBuilder cannot(String msg) {
        sb.append("Cannot " + msg);
        return this;
    }

    public static ErrorMessageBuilder of(AnnotatedElement e) {
        ErrorMessageBuilder emb = new ErrorMessageBuilder();
        emb.sb.append(e.toString());
        return emb;
    }

    public static ErrorMessageBuilder of(VariableDescriptor vd) {
        ErrorMessageBuilder emb = new ErrorMessageBuilder();
        if (vd instanceof FieldDescriptor) {
            emb.sb.append("field " + vd.getName());
        }
        return emb;
    }

    public ErrorMessageBuilder toResolve(String msg) {
        sb.append("To resolve " + msg);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public int length() {
        return sb.length();
    }

    /** {@inheritDoc} */
    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    /** {@inheritDoc} */
    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }
}
