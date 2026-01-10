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
package app.packed.lifecycle.runtime.errorhandling;

import static internal.app.packed.util.StringFormatter.format;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;

import app.packed.binding.Variable;

/**
 *
 */
public final class ErrorMessageBuilder implements CharSequence {

    StringBuilder sb = new StringBuilder();

    private ErrorMessageBuilder() {}

    public ErrorMessageBuilder cannot(String msg) {
        sb.append("Cannot " + msg);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    /** {@inheritDoc} */
    @Override
    public int length() {
        return sb.length();
    }

    /** {@inheritDoc} */
    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    public ErrorMessageBuilder toResolve(String msg) {
        sb.append("To resolve " + msg);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public static ErrorMessageBuilder of(Class<?> e) {
        ErrorMessageBuilder emb = new ErrorMessageBuilder();
        emb.sb.append(format(e));
        return emb;
    }

    public static ErrorMessageBuilder of(AnnotatedElement e) {
        ErrorMessageBuilder emb = new ErrorMessageBuilder();
        emb.sb.append(e.toString());
        return emb;
    }

    public static ErrorMessageBuilder of(Parameter p) {
        ErrorMessageBuilder emb = new ErrorMessageBuilder();
        emb.sb.append("parameter " + p.getName());
        return emb;
    }

    public static ErrorMessageBuilder of(Variable v) {
        ErrorMessageBuilder emb = new ErrorMessageBuilder();
        emb.sb.append("variable " + v);
        return emb;
    }
}
