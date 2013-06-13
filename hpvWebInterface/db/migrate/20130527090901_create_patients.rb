class CreatePatients < ActiveRecord::Migration
  def change
    create_table :patients, :id => false do |t|
      t.uuid :id, :primary_key => true
      t.string :local_id
      t.string :family_name
      t.string :given_name
      t.string :mothers_name
      t.string :fathers_name
      t.string :phone_number
      t.boolean :sms_reminders
      t.string :address
      t.string :area
      t.date :birth_day
      t.string :notes
      t.date :first_dose_date
      t.date :second_dose_date
      t.date :third_dose_date

      t.timestamps
    end
  end
end
