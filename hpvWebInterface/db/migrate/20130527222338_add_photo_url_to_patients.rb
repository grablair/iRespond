class AddPhotoUrlToPatients < ActiveRecord::Migration
  def change
    add_column(:patients, :photo_url, :string)
  end
end
