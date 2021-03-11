## Named

Named elements are references that can resolve to another element (usage -> declaration), be renamed or use in the
findUsage.

#### Reference

To add a new element to be used as a reference

* Add a named PsiNameIdentifierOwner in psi/named
* Add an implementation class psi/impl that CREATES the reference
* Add a reference class psi/references that RESOLVES the reference to the references PSI element

Example: A variable will reference its declaration. The reference is one-directional, from the usage to the declaration

#### Find usage

To search for the usage of a variable, the named element have to be added to the word scanner in the
OMTFindUsageProvider. This will index all elements and call a 'isReferenceTo' on identically named elements. For this
reason it's important to have a getName() and getNameIdentifier() method available in the NamedElement. For variables
this would not give a problem but for instance the command calls use an @ prefix in their call and the getName will make
sure the call getNameIdentifier() resolves to the prefix-less version so it matches with the actual command name.

* Add the TokenType to the OMTFindUsageProvider TokenSet that is used by the default word scanner
* Make sure the getNameIdentifier() in the usage and declaration resolve to identical names
* The getName() and getNameIdentifier() are added to the PsiImplUtil which is used by the class generation of the
  grammar (bnf)

#### Refactor-rename

* Add the setName() to the named element and PsiImplUtil
* Override the handleElementRename method in the Reference of the element.



