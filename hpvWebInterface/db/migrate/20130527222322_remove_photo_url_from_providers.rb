class RemovePhotoUrlFromProviders < ActiveRecord::Migration
  def change
    remove_column(:providers, :photo_url)
  end
end
