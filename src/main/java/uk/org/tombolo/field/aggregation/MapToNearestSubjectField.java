package uk.org.tombolo.field.aggregation;

import uk.org.tombolo.core.Subject;
import uk.org.tombolo.core.SubjectType;
import uk.org.tombolo.core.utils.SubjectTypeUtils;
import uk.org.tombolo.core.utils.SubjectUtils;
import uk.org.tombolo.recipe.FieldRecipe;
import uk.org.tombolo.field.*;

import java.util.Collections;
import java.util.List;

/**
 * MapToNearestSubjectField.java
 * This field will find the nearest subject of a given SubjectType and then
 * evaluate the fieldSpec with that new subject. For example, if the
 * nearestSubjectType is 'Street' and it is given a subject representing a
 * building, it will evaluate the fieldSpec with a subject representing the
 * Street that building is on (notwithstanding oddities in the data)
 */
public class MapToNearestSubjectField extends AbstractField implements ParentField {
    //TOASK how was the radius set?
    //this will take the radius in degrees, default unit of default projection
    // number chosen empirically
    private static final Double DEFAULT_MAX_RADIUS = 0.01;

    //TOASK has this been changed in a PR?
    private final String nearestSubjectProvider;
    private final String nearestSubjectType;
    private final FieldRecipe field;
    private Double maxRadius;
    private SingleValueField singleValueField;
    private SubjectType nearestSubjectTypeObject;

    MapToNearestSubjectField(String label, String nearestSubjectProvider, String nearestSubjectType, Double maxRadius, FieldRecipe field) {
        super(label);
        this.maxRadius = maxRadius;
        this.nearestSubjectProvider = nearestSubjectProvider;
        this.nearestSubjectType = nearestSubjectType;
        this.field = field;
    }

    public void initialize() {
        nearestSubjectTypeObject = SubjectTypeUtils.getSubjectTypeByProviderAndLabel(nearestSubjectProvider, nearestSubjectType);

        // Initialize maxRadius with a default value
        if (null == maxRadius) maxRadius = DEFAULT_MAX_RADIUS;
        try {
            this.singleValueField = (SingleValueField) field.toField();
            singleValueField.setFieldCache(fieldCache);
        } catch (ClassNotFoundException e) {
            throw new Error("Field not valid");
        }
    }

    private Subject getSubjectProximalToSubject(Subject subject) throws IncomputableFieldException {
        Subject nearestSubject = SubjectUtils.subjectNearestSubject(nearestSubjectTypeObject, subject, maxRadius);
        if (nearestSubject == null) {
            throw new IncomputableFieldException(String.format(
                    "Subject %s has no nearby subjects of type %s, but should have 1",
                    subject.getName(),
                    nearestSubjectType));
        }

        return nearestSubject;
    }

    @Override
    public List<Field> getChildFields() {
        if (null == singleValueField) { initialize(); }
        return Collections.singletonList(singleValueField);
    }

    @Override
    public String valueForSubject(Subject subject, Boolean timeStamp) throws IncomputableFieldException {
        if (null == singleValueField) { initialize(); }
        return singleValueField.valueForSubject(
                getSubjectProximalToSubject(subject), timeStamp);
    }
}
