/**
 */
package com.vanderhighway.grrbac.model.grrbac.model;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Cardinality RD Constraint</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.vanderhighway.grrbac.model.grrbac.model.CardinalityRDConstraint#getBound <em>Bound</em>}</li>
 * </ul>
 *
 * @see com.vanderhighway.grrbac.model.grrbac.model.GRRBACPackage#getCardinalityRDConstraint()
 * @model
 * @generated
 */
public interface CardinalityRDConstraint extends UnaryDemarcationConstraint {
	/**
	 * Returns the value of the '<em><b>Bound</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bound</em>' attribute.
	 * @see #setBound(Integer)
	 * @see com.vanderhighway.grrbac.model.grrbac.model.GRRBACPackage#getCardinalityRDConstraint_Bound()
	 * @model dataType="org.eclipse.emf.ecore.xml.type.IntObject"
	 * @generated
	 */
	Integer getBound();

	/**
	 * Sets the value of the '{@link com.vanderhighway.grrbac.model.grrbac.model.CardinalityRDConstraint#getBound <em>Bound</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bound</em>' attribute.
	 * @see #getBound()
	 * @generated
	 */
	void setBound(Integer value);

} // CardinalityRDConstraint
