package de.goddchen.android.astrometry.data;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Goddchen on 12.03.14.
 */
@DatabaseTable
public class Job implements Serializable {

    @DatabaseField(id = true, generatedId = false)
    public Long id;
    @DatabaseField
    public String status;
    @DatabaseField
    public String original_filename;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public String[] machine_tags;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public String[] tags;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public String[] objects_in_field;
    @DatabaseField
    public long submissionID;
    @DatabaseField
    public long imageID;

    public Job() {

    }

//    @DatabaseField
//    public Map<String, String> calibration;

    @Override
    public String toString() {
        return id + " (" + status + "): " +
                Arrays.toString(objects_in_field);
    }

}
