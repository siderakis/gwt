/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.jdt;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Walks the AST to determine every location from which a type is referenced.
 */
public abstract class TypeRefVisitor extends ASTVisitor {

  @Override
  public void endVisit(ArrayQualifiedTypeReference x, BlockScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(ArrayQualifiedTypeReference x, ClassScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(ArrayTypeReference x, BlockScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(ArrayTypeReference x, ClassScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(MessageSend messageSend, BlockScope scope) {
    if (messageSend.binding.isStatic()) {
      maybeDispatch(scope, messageSend, messageSend.actualReceiverType);
    }
  }

  @Override
  public void endVisit(ParameterizedQualifiedTypeReference x, BlockScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(ParameterizedQualifiedTypeReference x, ClassScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(ParameterizedSingleTypeReference x, BlockScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(ParameterizedSingleTypeReference x, ClassScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(QualifiedNameReference x, BlockScope scope) {
    if (x.binding instanceof FieldBinding) {
      maybeDispatch(scope, x, x.fieldBinding().declaringClass);
    }
  }

  @Override
  public void endVisit(QualifiedTypeReference x, BlockScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(QualifiedTypeReference x, ClassScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(SingleTypeReference x, BlockScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(SingleTypeReference x, ClassScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(Wildcard x, BlockScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @Override
  public void endVisit(Wildcard x, ClassScope scope) {
    maybeDispatch(scope, x, x.resolvedType);
  }

  @SuppressWarnings("unused")
  protected void onBinaryTypeRef(BinaryTypeBinding referencedType,
      CompilationUnitDeclaration unitOfReferrer, Expression expression) {
  }

  protected abstract void onTypeRef(SourceTypeBinding referencedType,
      CompilationUnitDeclaration unitOfReferrer);

  private CompilationUnitScope findUnitScope(Scope referencedFrom) {
    assert (referencedFrom != null);
    Scope scope = referencedFrom;
    while (scope.parent != null) {
      scope = scope.parent;
    }
    assert (scope instanceof CompilationUnitScope);
    return (CompilationUnitScope) scope;
  }

  private void maybeDispatch(Scope referencedFrom, Expression expression,
      TypeBinding binding) {
    assert (referencedFrom != null);
    assert (binding != null);

    if (binding instanceof SourceTypeBinding) {
      SourceTypeBinding type = (SourceTypeBinding) binding;
      CompilationUnitScope from = findUnitScope(referencedFrom);
      onTypeRef(type, from.referenceContext);
    } else if (binding instanceof ArrayBinding) {
      maybeDispatch(referencedFrom, expression,
          ((ArrayBinding) binding).leafComponentType);
    } else if (binding instanceof BinaryTypeBinding) {
      CompilationUnitScope from = findUnitScope(referencedFrom);
      onBinaryTypeRef((BinaryTypeBinding) binding, from.referenceContext,
          expression);
    } else if (binding instanceof ParameterizedTypeBinding) {
      // Make sure that we depend on the generic version of the class.
      ParameterizedTypeBinding ptBinding = (ParameterizedTypeBinding) binding;
      maybeDispatch(referencedFrom, expression, ptBinding.genericType());
    } else if (binding instanceof RawTypeBinding) {
      // Make sure that we depend on the generic version of the class.
      RawTypeBinding rawTypeBinding = (RawTypeBinding) binding;
      maybeDispatch(referencedFrom, expression, rawTypeBinding.genericType());
    } else {
      // We don't care about other cases.
    }
  }
}
