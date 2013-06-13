class AddPhotoToPatients < ActiveRecord::Migration
  def change
    add_column :providers, :photo_url, :string
  end
end
